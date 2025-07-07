package org.octopusden.octopus.releasemanagementservice.service.impl

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import org.joda.time.DateTime
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException
import org.octopusden.octopus.releasemanagementservice.config.JiraClientConfig
import org.octopusden.octopus.releasemanagementservice.service.JiraService
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JiraServiceImpl(jiraClientConfig: JiraClientConfig): JiraService {
    private val client: JiraRestClient = jiraClientConfig.jiraRestClient()

    private val issueTypes: Map<String, IssueType> by lazy { client.metadataClient.issueTypes.claim().associateBy { it.name } }

    private val fields: Map<String, String> by lazy { client.metadataClient.fields.claim().associate { it.name to it.id } }

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
            val field = getField(fieldName)
            builder.setFieldValue(field, value)
        }
        val input = builder.build()
        return client.issueClient.createIssue(input).claim().key
    }

    private fun getIssueType(name: String): IssueType = issueTypes[name] ?: throw NotFoundException("Issues type '$name' is not found!")

    private fun getField(name: String): String = fields[name] ?: throw NotFoundException("Field '$name' is not found!")

    companion object {
        const val EPIC_ISSUE = "Epic"
        const val MANDATORY_UPDATE_ISSUE = "Mandatory Update"
        const val CUSTOMER_FIELD = "Customer"
        const val EPIC_NAME_FIELD = "Epic Name"
        const val EPIC_LINK_FIELD = "Epic Link"
    }
}