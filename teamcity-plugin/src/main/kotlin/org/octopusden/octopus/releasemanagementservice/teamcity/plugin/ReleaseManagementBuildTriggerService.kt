package org.octopusden.octopus.releasemanagementservice.teamcity.plugin

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
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildStatus
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider

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

    override fun getDefaultTriggerProperties() = mutableMapOf(BRANCH to "")

    override fun getTriggerPropertiesProcessor() = PropertiesProcessor { properties ->
        val invalidProperties = mutableListOf<InvalidProperty>()
        if (properties[SERVICE_URL].isNullOrBlank()) {
            invalidProperties.add(InvalidProperty(SERVICE_URL, "Service URL must be defined"))
        }
        if (properties[COMPONENT].isNullOrBlank()) {
            invalidProperties.add(InvalidProperty(COMPONENT, "Component must be defined"))
        }
        /*TODO:
         * check SERVICE_URL is valid URL of release management service
         * check COMPONENT is existing component ID
         */
        invalidProperties
    }

    override fun getBuildTriggeringPolicy() = object : PolledBuildTrigger() {
        override fun triggerBuild(context: PolledTriggerContext) {
            val serviceUrl = context.triggerDescriptor.properties[SERVICE_URL]
            val component = context.triggerDescriptor.properties[COMPONENT]
            val latestVersion = try {
                ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                    override fun getApiUrl() = serviceUrl!!
                    override fun getTimeRetryInMillis() = 1000
                }).getBuilds(
                    component!!,
                    BuildFilterDTO(
                        statuses = setOf(BuildStatus.RELEASE),
                        descending = true,
                        limit = 1
                    )
                ).firstOrNull()?.version
            } catch (e: Exception) {
                throw BuildTriggerException("Unable to retrieve latest RELEASE version of '$component' from '$serviceUrl'", e)
            }
            val previousVersion = context.customDataStorage.getValue(VERSION)
            when (latestVersion) {
                null ->
                    log.info("No RELEASE version of '$component' found. Skip build triggering")

                previousVersion ->
                    log.info("Latest RELEASE version '$latestVersion' of '$component' has not been changed. Skip build triggering")

                else -> {
                    val triggeredBy = "$displayName on changing of RELEASE version from '$previousVersion' to '$latestVersion'"
                    val branch = context.triggerDescriptor.properties[BRANCH]
                    val queuedBuild = if (branch.isNullOrBlank()) {
                        context.buildType.addToQueue(triggeredBy)
                    } else {
                        buildCustomizerFactory.createBuildCustomizer(context.buildType, null).apply {
                            setDesiredBranchName(branch)
                        }.createPromotion().addToQueue(triggeredBy)
                    }
                    if (queuedBuild == null) {
                        log.warn("Unable to queue build on changing of RELEASE version of '$component' from '$previousVersion' to '$latestVersion'")
                    } else {
                        context.customDataStorage.putValue(VERSION, latestVersion)
                    }
                }
            }
        }
    }

    companion object {
        const val DESCRIPTION = "Trigger build when detecting new RELEASE version of component in release management"

        //Basic settings
        const val SERVICE_URL = "release.management.build.trigger.service.url"
        const val COMPONENT = "release.management.build.trigger.component"

        //Advanced settings
        const val BRANCH = "release.management.build.trigger.branch"

        const val VERSION = "release.management.build.trigger.version"

        val log = Logger.getInstance(ReleaseManagementBuildTriggerService::class.java)
    }
}