package org.octopusden.octopus.releasemanagementservice.service.impl

import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClient
import org.octopusden.octopus.releasemanagementservice.service.IssueService
import org.springframework.stereotype.Service

@Service
class IssueServiceImpl(private val client: LegacyRelengClient) : IssueService {
    override fun getIssueReleases(issueKey: String) = client.getIssueReleases(issueKey)
}
