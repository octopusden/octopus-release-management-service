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
import jetbrains.buildServer.serverSide.BuildCustomizerFactory
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider
import org.octopusden.octopus.releasemanagementservice.teamcity.plugin.dto.BuildSelectionDTO

class ReleaseManagementBuildTriggerService(
    private val pluginDescriptor: PluginDescriptor,
    private val buildCustomizerFactory: BuildCustomizerFactory
) : BuildTriggerService() {
    override fun getName() = "release-management-teamcity-build-trigger"

    override fun getDisplayName() = "Release Management TeamCity Build Trigger"

    override fun describeTrigger(trigger: BuildTriggerDescriptor) = DESCRIPTION

    override fun isMultipleTriggersPerBuildTypeAllowed() = true

    override fun supportsBuildCustomization() = true

    override fun getEditParametersUrl() =
        pluginDescriptor.getPluginResourcesPath("editReleaseManagementBuildTriggerParameters.jsp")

    override fun getDefaultTriggerProperties() = mutableMapOf(BRANCH to "", POLL_INTERVAL to "300")

    override fun getTriggerPropertiesProcessor() = PropertiesProcessor { properties ->
        val invalidProperties = mutableListOf<InvalidProperty>()
        val serviceUrl = properties[SERVICE_URL]
        if (serviceUrl.isNullOrBlank()) {
            invalidProperties.add(InvalidProperty(SERVICE_URL, "Service URL must be defined"))
        } else {
            try {
                createClient(serviceUrl).getServiceInfo()
            } catch (e: Exception) {
                invalidProperties.add(InvalidProperty(SERVICE_URL, e.message))
            }
        }
        val selections = properties[SELECTIONS]
        if (selections.isNullOrBlank()) {
            invalidProperties.add(InvalidProperty(SELECTIONS, "Selections must be defined"))
        } else {
            try {
                if (mapper.readValue(selections, object : TypeReference<Set<BuildSelectionDTO>>() {}).isEmpty()) {
                    throw NoSuchElementException("Selections is empty")
                }
            } catch (e: Exception) {
                invalidProperties.add(InvalidProperty(SELECTIONS, e.message))
            }
        }
        invalidProperties
    }

    override fun getBuildTriggeringPolicy() = object : PolledBuildTrigger() {
        override fun getPollInterval(context: PolledTriggerContext) =
            context.triggerDescriptor.properties[POLL_INTERVAL]?.toIntOrNull() ?: super.getPollInterval(context)

        override fun triggerBuild(context: PolledTriggerContext) {
            val client = createClient(context.triggerDescriptor.properties[SERVICE_URL]!!)
            val currentVersions = mapper.readValue(
                context.triggerDescriptor.properties[SELECTIONS]!!,
                object : TypeReference<Set<BuildSelectionDTO>>() {}
            ).associateWith {
                try {
                    client.getBuilds(it.component, it.toBuildFilterDTO()).first().version
                } catch (e: Exception) {
                    throw BuildTriggerException(
                        "Unable to retrieve latest version of '${it.component}' with status no less than ${it.status}" +
                                if (it.minor == null) "" else " and minor equals ${it.minor}"
                    )
                }
            }
            val previousVersions = context.customDataStorage.getValue(VERSIONS)?.let {
                mapper.readValue(it, object : TypeReference<Map<BuildSelectionDTO, String>>() {})
            } ?: emptyMap()
            val diff = (currentVersions.entries - previousVersions.entries).map {
                "- new version ${it.value} detected for '${it.key.component}' (status >= ${it.key.status}" +
                        if (it.key.minor == null) ")" else ", minor == ${it.key.minor})"
            }
            if (diff.isNotEmpty()) {
                log.debug(diff.joinToString("\n", "Triggering build on following changes:\n"))
                val branch = context.triggerDescriptor.properties[BRANCH]
                if (branch.isNullOrBlank()) {
                    context.buildType.addToQueue(displayName)
                } else {
                    buildCustomizerFactory.createBuildCustomizer(context.buildType, null).apply {
                        setDesiredBranchName(branch)
                    }.createPromotion().addToQueue(displayName)
                }
                context.customDataStorage.putValue(VERSIONS, mapper.writeValueAsString(currentVersions))
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

        const val VERSIONS = "release.management.build.trigger.version"

        private val log = Logger.getInstance(ReleaseManagementBuildTriggerService::class.java)

        private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

        private fun createClient(serviceUrl: String) =
            ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = serviceUrl
                override fun getTimeRetryInMillis() = 1000
            })
    }
}