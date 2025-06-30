package org.octopusden.octopus.releasemanagementservice.service.impl

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.Project
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import org.octopusden.octopus.releasemanagementservice.config.JiraClientConfig
import org.octopusden.octopus.releasemanagementservice.service.JiraService
import org.springframework.stereotype.Service

@Service
class JiraServiceImpl(jiraClientConfig: JiraClientConfig): JiraService {
    private val client: JiraRestClient = jiraClientConfig.jiraRestClient()

    override fun createJiraIssue() {
        val issueInput = IssueInputBuilder(

        )

        TODO("Not yet implemented.")
    }

    private fun getIssueType(name: String): IssueType {
        return client.metadataClient.issueTypes.claim()
            .first { it.name == name }
    }

    private fun getProject(key: String): Project {
        return client.projectClient.getProject(key).claim()
    }
}