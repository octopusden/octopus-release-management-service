package org.octopusden.octopus.releasemanagementservice.service.impl

import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.exception.AlreadyExistsException
import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClient
import org.octopusden.octopus.releasemanagementservice.service.ComponentRegistryService
import org.octopusden.octopus.releasemanagementservice.service.JiraService
import org.octopusden.octopus.releasemanagementservice.service.UtilityService
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.CUSTOMER_FIELD
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.EPIC_ISSUE
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.EPIC_LINK_FIELD
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.EPIC_NAME_FIELD
import org.octopusden.octopus.releasemanagementservice.service.impl.JiraServiceImpl.Companion.MANDATORY_UPDATE_ISSUE
import org.springframework.stereotype.Service
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

@Service
class UtilityServiceImpl(
    private val legacyRelengClient: LegacyRelengClient,
    private val jiraService: JiraService,
    private val componentRegistryService: ComponentRegistryService
): UtilityService {
    override fun createMandatoryUpdate(dryRun: Boolean, dto: MandatoryUpdateDTO): MandatoryUpdateResponseDTO {
        val builds = legacyRelengClient.getMandatoryUpdateBuilds(dto.component, dto.version, dto.filter.activeLinePeriod)
            .filter {
                if (dto.filter.excludeComponents.contains(it.component)) return@filter false
                val foundComponent = componentRegistryService.getById(it.component)
                foundComponent.distribution?.external == true && foundComponent.system.intersect(dto.filter.excludeSystems).isEmpty()
            }
            .map { it.toShortBuildDTO() }
        if (builds.isEmpty() || dryRun) {
            return MandatoryUpdateResponseDTO(null, builds)
        }
        val epicKey = createEpic(dto.component, dto.version, dto)
        createSubIssues(dto.component, dto.version, builds, dto, epicKey)
        return MandatoryUpdateResponseDTO(epicKey, builds)
    }

    private fun createEpic(component: String, version: String, dto: MandatoryUpdateDTO): String {
        val summary = EPIC_SUMMARY_TEMPLATE.format(component, version)
        if (epicExists(summary)) {
            throw AlreadyExistsException("Epic for bump dependencies $component:$version already exists!")
        }
        val description = buildString {
            append(EPIC_DESCRIPTION_TEMPLATE.format(component, version))
            if (dto.notice.isNotBlank()) {
                append("\nNotice: ").append(dto.notice)
            }
        }
        val assignee = componentRegistryService.getById(component).let { it.releaseManager ?: it.componentOwner }
        val extraFields = mapOf(CUSTOMER_FIELD to multiSelectOf(dto.customer), EPIC_NAME_FIELD to dto.epicName)
        return jiraService.createIssue(
            dto.projectKey,
            EPIC_ISSUE,
            summary,
            description,
            assignee,
            dto.dueDate,
            extraFields
        )
    }

    private fun createSubIssues(component: String, version: String, builds: List<ShortBuildDTO>, dto: MandatoryUpdateDTO, epicKey: String) {
        val buildsByComponent = builds.groupBy { it.component }
        val extraFields = mapOf(CUSTOMER_FIELD to multiSelectOf(dto.customer), EPIC_LINK_FIELD to epicKey)
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

    private fun epicExists(summary: String): Boolean {
        val escapedSummary = jqlQuote(summary)
        val jql = """
            issueType = "$EPIC_ISSUE"
            AND summary ~ "\"$escapedSummary\""
        """.trimIndent()
        return jiraService.findIssues(jql).any { it.summary == summary }
    }

    private fun jqlQuote(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun multiSelectOf(vararg values: String): List<ComplexIssueInputFieldValue> =
        values.map { ComplexIssueInputFieldValue(mapOf("value" to it)) }

    private fun BuildDTO.toShortBuildDTO(): ShortBuildDTO =
        ShortBuildDTO(component = component, version = version, status = status)

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