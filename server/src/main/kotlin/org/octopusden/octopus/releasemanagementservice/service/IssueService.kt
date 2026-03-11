package org.octopusden.octopus.releasemanagementservice.service

import org.octopusden.octopus.releasemanagementservice.client.common.dto.IssueReleasesDTO

interface IssueService {
    fun getIssueReleases(issueKey: String): IssueReleasesDTO
}
