package org.octopusden.octopus.releasemanagementservice.teamcity.plugin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor
import jetbrains.buildServer.buildTriggers.BuildTriggerException
import jetbrains.buildServer.buildTriggers.BuildTriggerService
import jetbrains.buildServer.buildTriggers.PolledBuildTrigger
import jetbrains.buildServer.buildTriggers.PolledTriggerContext
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.BuildQueue
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider
import org.octopusden.octopus.releasemanagementservice.teamcity.plugin.dto.BuildSelectionDTO
import org.octopusden.octopus.releasemanagementservice.teamcity.plugin.dto.VersionDTO

class ReleaseManagementBuildTriggerService(
    private val pluginDescriptor: PluginDescriptor, private val buildQueue: BuildQueue
) : BuildTriggerService() {
    override fun getName() = "release-management-teamcity-build-trigger"

    override fun getDisplayName() = "Release Management TeamCity Build Trigger"

    override fun describeTrigger(trigger: BuildTriggerDescriptor) = DESCRIPTION

    override fun isMultipleTriggersPerBuildTypeAllowed() = true

    override fun supportsBuildCustomization() = true

    override fun getEditParametersUrl() =
        pluginDescriptor.getPluginResourcesPath("editReleaseManagementBuildTriggerParameters.jsp")

    override fun getDefaultTriggerProperties() = mutableMapOf(POLL_INTERVAL to "300")

    override fun getTriggerPropertiesProcessor() = PropertiesProcessor { properties ->
        val invalidProperties = mutableListOf<InvalidProperty>()
        val serviceUrlProperty = properties[SERVICE_URL]?.trim()
        if (serviceUrlProperty.isNullOrEmpty()) {
            invalidProperties.add(InvalidProperty(SERVICE_URL, "Service URL must be defined"))
        } else if (!ReferencesResolverUtil.mayContainReference(serviceUrlProperty)) {
            try {
                createClient(serviceUrlProperty).getServiceInfo()
            } catch (e: Exception) {
                invalidProperties.add(InvalidProperty(SERVICE_URL, "Invalid Service URL"))
            }
        }
        val selectionsProperty = properties[SELECTIONS]?.trim()
        if (selectionsProperty.isNullOrEmpty()) {
            invalidProperties.add(InvalidProperty(SELECTIONS, "Selections must be defined"))
        } else if (!ReferencesResolverUtil.mayContainReference(selectionsProperty)) {
            try {
                mapper.readValue(selectionsProperty, object : TypeReference<Set<BuildSelectionDTO>>() {})
            } catch (e: Exception) {
                invalidProperties.add(InvalidProperty(SELECTIONS, "Unable to parse Selections"))
            }
        }
        invalidProperties
    }

    override fun getBuildTriggeringPolicy() = object : PolledBuildTrigger() {
        override fun getPollInterval(context: PolledTriggerContext) =
            context.triggerDescriptor.properties[POLL_INTERVAL]?.trim()?.toIntOrNull() ?: super.getPollInterval(context)

        override fun triggerBuild(context: PolledTriggerContext) {
            val serviceUrlProperty = context.triggerDescriptor.properties[SERVICE_URL]?.trim()
            val client = if (serviceUrlProperty.isNullOrEmpty()) {
                throw BuildTriggerException("Service URL is not defined")
            } else {
                try {
                    createClient(serviceUrlProperty).also { it.getServiceInfo() }
                } catch (e: Exception) {
                    throw BuildTriggerException("Invalid Service URL", e)
                }
            }
            val selectionsProperty = context.triggerDescriptor.properties[SELECTIONS]?.trim()
            val selections = if (selectionsProperty.isNullOrEmpty()) {
                throw BuildTriggerException("Selections are not defined")
            } else {
                try {
                    mapper.readValue(selectionsProperty, object : TypeReference<Set<BuildSelectionDTO>>() {})
                } catch (e: Exception) {
                    throw BuildTriggerException("Unable to parse Selections", e)
                }
            }
            val currentVersions = selections.map {
                try {
                    VersionDTO(it, client.getBuilds(it.component, it.toBuildFilterDTO()).first().version)
                } catch (e: Exception) {
                    throw BuildTriggerException(
                        "Unable to retrieve latest version of '${it.component}' with status no less than ${it.status}" + if (it.minor == null) "" else " and minor equals ${it.minor}",
                        e
                    )
                }
            }
            val previousVersions = context.customDataStorage.getValue(VERSIONS)?.let {
                try {
                    mapper.readValue(it, object : TypeReference<Set<VersionDTO>>() {})
                } catch (e: Exception) {
                    log.warn("Unable to parse stored versions", e)
                    null
                }
            } ?: emptySet()
            val diff = (currentVersions - previousVersions).map {
                "${it.version} ['${it.selection.component}'|${it.selection.status}" + if (it.selection.minor == null) "]" else "|${it.selection.minor}]"
            }
            if (diff.isNotEmpty()) {
                val triggeredBy = diff.joinToString(", ", "$displayName on following changes: ").let {
                    if (it.length < 257) it else "${it.take(253)}..."
                }
                val branch = context.triggerDescriptor.properties[BRANCH]?.trim()
                val previousQueuedBuild = context.customDataStorage.getValue(QUEUED_BUILD)
                if (context.triggerDescriptor.properties[QUEUE_OPTIMIZATION].toBoolean() && previousQueuedBuild != null) {
                    buildQueue.findQueued(previousQueuedBuild)?.removeFromQueue(null, "Dismissed by $displayName")
                }
                val queuedBuild = context.createBuildCustomizer(null).apply {
                    if (!branch.isNullOrEmpty()) setDesiredBranchName(branch)
                }.createPromotion().addToQueue(triggeredBy)
                if (queuedBuild != null) {
                    context.customDataStorage.putValue(VERSIONS, mapper.writeValueAsString(currentVersions))
                    context.customDataStorage.putValue(QUEUED_BUILD, queuedBuild.itemId)
                    context.customDataStorage.flush()
                } else {
                    log.warn("Unable to queue build triggered by $triggeredBy")
                }
            }
        }
    }

    companion object {
        const val DESCRIPTION = "Trigger build when detecting new versions of components in release management"

        //Basic settings
        const val SERVICE_URL = "release.management.build.trigger.service.url"
        const val SELECTIONS = "release.management.build.trigger.selections"

        //Advanced settings
        const val BRANCH = "release.management.build.trigger.branch"
        const val POLL_INTERVAL = "release.management.build.trigger.poll.interval"
        const val QUEUE_OPTIMIZATION = "release.management.build.trigger.queue.optimization"

        //Custom data
        const val VERSIONS = "release.management.build.trigger.versions"
        const val QUEUED_BUILD = "release.management.build.trigger.queued.build"

        private val log = Logger.getInstance(ReleaseManagementBuildTriggerService::class.java)

        private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

        private fun createClient(serviceUrl: String) =
            ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = serviceUrl
                override fun getTimeRetryInMillis() = 1000
            })
    }
}