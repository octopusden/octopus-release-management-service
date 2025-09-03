package org.octopusden.octopus.releasemanagementservice.service

import com.atlassian.jira.rest.client.api.domain.Issue
import java.util.Date

interface JiraService {
    fun createIssue(
        projectKey: String,
        issueTypeName: String,
        summary: String,
        description: String,
        assignee: String,
        dueDate: Date,
        extraFields: Map<String, Any>
    ): String

    fun findIssues(jql: String): List<Issue>
}