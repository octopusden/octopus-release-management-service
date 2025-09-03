package org.octopusden.octopus.releasemanagementservice.service.impl

import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.Field
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import org.joda.time.DateTime
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException
import org.octopusden.octopus.releasemanagementservice.config.JiraClientProperties
import org.octopusden.octopus.releasemanagementservice.service.JiraService
import org.springframework.stereotype.Service
import java.net.URI
import java.util.Date

@Service
class JiraServiceImpl(jiraClientProperties: JiraClientProperties): JiraService {
    private val client = AsynchronousJiraRestClientFactory()
        .createWithBasicHttpAuthentication(URI(jiraClientProperties.host), jiraClientProperties.username, jiraClientProperties.password)

    override fun createIssue(
        projectKey: String,
        issueTypeName: String,
        summary: String,
        description: String,
        assignee: String,
        dueDate: Date,
        extraFields: Map<String, Any>
    ): String {
        val builder = IssueInputBuilder()
            .setProjectKey(projectKey)
            .setIssueType(getIssueType(issueTypeName))
            .setSummary(summary)
            .setDescription(description)
            .setAssigneeName(assignee)
            .setDueDate(DateTime(dueDate))
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
    }
}