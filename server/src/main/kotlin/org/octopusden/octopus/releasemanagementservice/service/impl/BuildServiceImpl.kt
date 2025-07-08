package org.octopusden.octopus.releasemanagementservice.service.impl

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClient
import org.octopusden.octopus.releasemanagementservice.service.BuildService
import org.octopusden.octopus.releasemanagementservice.service.ComponentRegistryService
import org.octopusden.octopus.releasemanagementservice.service.JiraService
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.CUSTOMER_FIELD
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.MANDATORY_UPDATE_ISSUE
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.EPIC_NAME_FIELD
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.EPIC_LINK_FIELD
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.EPIC_ISSUE
import org.springframework.stereotype.Service
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@Service
class BuildServiceImpl(
    private val client: LegacyRelengClient,
    private val jiraService: JiraService,
    private val componentRegistryService: ComponentRegistryService
) : BuildService {
    override fun getBuilds(component: String, filter: BuildFilterDTO): Collection<ShortBuildDTO> = client.getBuilds(component, filter)

    override fun getBuild(component: String, version: String): BuildDTO = client.getBuild(component, version)

    override fun createMandatoryUpdate(
        component: String,
        version: String,
        dryRun: Boolean,
        dto: MandatoryUpdateDTO
    ): MandatoryUpdateResponseDTO {
        val excludes = dto.filter.excludeComponents
        val systems = dto.filter.systems
        val builds = client.getMandatoryUpdateBuilds(component, version, dto.filter.activeLinePeriod)
            .filter {
                if (excludes.contains(it.component)) return@filter false
                val comp = componentRegistryService.getById(it.component)
                comp.system.intersect(systems).isEmpty() && comp.distribution?.external == true
            }
        if (builds.isEmpty()) {
            return MandatoryUpdateResponseDTO(null, null)
        }
        if (dryRun) {
            return MandatoryUpdateResponseDTO(null, builds)
        }
        try {
            val epicKey = createEpic(component, version, dto)
            createSubIssues(component, version, builds, dto, epicKey)
            return MandatoryUpdateResponseDTO(epicKey, null)
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    private fun createEpic(component: String, version: String, dto: MandatoryUpdateDTO): String {
        val description = buildString {
            append(EPIC_DESCRIPTION_TEMPLATE.format(component, version))
            if (dto.notice.isNotBlank()) {
                append(" Notice: ").append(dto.notice)
            }
        }
        val assignee = componentRegistryService.getById(component).let { it.releaseManager ?: it.componentOwner }
        val extraFields = mapOf(CUSTOMER_FIELD to ComplexIssueInputFieldValue(mapOf("value" to dto.customer)), EPIC_NAME_FIELD to dto.epicName)
        return jiraService.createIssue(
            dto.projectKey,
            EPIC_ISSUE,
            EPIC_SUMMARY_TEMPLATE.format(component, version),
            description,
            assignee,
            dto.dueDate,
            extraFields
        )
    }

    private fun createSubIssues(component: String, version: String, builds: List<BuildDTO>, dto: MandatoryUpdateDTO, epicKey: String) {
        val buildsByComponent = builds.groupBy { it.component }
        val extraFields = mapOf(CUSTOMER_FIELD to ComplexIssueInputFieldValue(mapOf("value" to dto.customer)), EPIC_LINK_FIELD to epicKey)
        for ((compId, compBuilds) in buildsByComponent) {
            val detailedComponent = componentRegistryService.getDetailedComponent(compId, compBuilds.first().version)
            val assignee = detailedComponent.componentOwner
            val currentProjectKey = detailedComponent.jiraComponentVersion.component.projectKey
            val versions = compBuilds.joinToString(separator = "\n") { "- ${it.version}" }
            jiraService.createIssue(
                currentProjectKey,
                MANDATORY_UPDATE_ISSUE,
                ISSUE_SUMMARY_TEMPLATE.format(compId, component, version),
                ISSUE_DESCRIPTION_TEMPLATE.format(compId, versions, component, version),
                assignee,
                dto.dueDate,
                extraFields
            )
        }
    }

    companion object {
        private const val EPIC_SUMMARY_TEMPLATE = "Bump Dependencies on %s %s"
        private const val EPIC_DESCRIPTION_TEMPLATE =
            "Bump Dependencies on %s to %s or a later version. Components eligible for update are listed in the epic's issues."
        private const val ISSUE_SUMMARY_TEMPLATE = "%s: Bump Dependencies on %s to %s or a later version."
        private const val ISSUE_DESCRIPTION_TEMPLATE =
            "Component %s has the following versions eligible for mandatory update:\n%s\n\n" +
                    "Those versions are to be updated: please bump dependencies on %s to %s or a later version."
    }
}