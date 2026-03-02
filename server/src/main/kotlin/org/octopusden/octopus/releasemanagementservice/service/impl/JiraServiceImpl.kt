package org.octopusden.octopus.releasemanagementservice.service.impl

import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.Field
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import feign.RequestInterceptor
import org.joda.time.DateTime
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.jira.JiraClassicClient
import org.octopusden.octopus.infrastructure.jira.dto.ProjectCategory
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException
import org.octopusden.octopus.releasemanagementservice.config.JiraClientProperties
import org.octopusden.octopus.releasemanagementservice.service.JiraService
import org.springframework.stereotype.Service
import java.net.URI
import java.util.Base64
import java.util.Date

@Service
class JiraServiceImpl(jiraClientProperties: JiraClientProperties): JiraService {
    private val client = AsynchronousJiraRestClientFactory()
        .createWithBasicHttpAuthentication(URI(jiraClientProperties.host), jiraClientProperties.username, jiraClientProperties.password)

    private val customClient = JiraClassicClient(
        object : ClientParametersProvider {
            override fun getApiUrl(): String = jiraClientProperties.host
            override fun getAuth(): CredentialProvider =
                object : CredentialProvider({
                    RequestInterceptor { template ->
                        val basic = Base64.getEncoder()
                            .encodeToString("${jiraClientProperties.username}:${jiraClientProperties.password}".toByteArray())
                        template.header("Authorization", "Basic $basic")
                    }
                }) {}
        }
    )

    override fun createIssue(
        projectKey: String,
        issueTypeName: String,
        summary: String,
        description: String,
        assignee: String,
        dueDate: Date?,
        extraFields: Map<String, Any>
    ): String {
        val builder = IssueInputBuilder()
            .setProjectKey(projectKey)
            .setIssueType(getIssueType(issueTypeName))
            .setSummary(summary)
            .setDescription(description)
            .setAssigneeName(assignee)
        if (dueDate != null) {
            builder.setDueDate(DateTime(dueDate))
        }
        extraFields.forEach { (fieldName, value) ->
            val fieldId = getField(fieldName).id
            builder.setFieldValue(fieldId, value)
        }
        val input = builder.build()
        return client.issueClient.createIssue(input).claim().key
    }

    override fun findIssues(jql: String): List<Issue> {
        return client.searchClient.searchJql(jql).claim().issues.toList()
    }

    override fun getProjectCategory(projectKey: String): ProjectCategory? {
        return customClient.getProject(projectKey).projectCategory
    }

    private fun getIssueType(name: String): IssueType {
        return client.metadataClient.issueTypes.claim().find { it.name == name } ?: throw NotFoundException("Issues type '$name' is not found!")
    }

    private fun getField(name: String): Field {
        return client.metadataClient.fields.claim().find { it.name == name } ?: throw NotFoundException("Field '$name' is not found!")
    }

    companion object {
        const val EPIC_ISSUE = "Epic"
        const val MANDATORY_UPDATE_ISSUE = "Mandatory Update"
        const val CUSTOMER_FIELD = "Customer"
        const val EPIC_NAME_FIELD = "Epic Name"
        const val EPIC_LINK_FIELD = "Epic Link"
        const val CRN_REQUIRED_FIELD = "CRN Required"
        const val DEVELOPMENT_PROJECT_CATEGORY = "Development"

        fun jqlQuote(value: String): String =
            value.replace("\\", "\\\\").replace("\"", "\\\"")

        fun multiSelectOf(vararg values: String): List<ComplexIssueInputFieldValue> =
            values.map { ComplexIssueInputFieldValue(mapOf("value" to it)) }

        fun singleSelectOf(value: String): ComplexIssueInputFieldValue =
            ComplexIssueInputFieldValue(mapOf("value" to value))
    }
}