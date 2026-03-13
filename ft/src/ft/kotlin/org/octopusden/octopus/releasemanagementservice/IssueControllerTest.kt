package org.octopusden.octopus.releasemanagementservice

class IssueControllerTest :
    BaseIssueControllerTest(),
    BaseReleaseManagementServiceFuncTest {
    override fun getObjectMapper() = TestUtil.mapper

    override fun getIssueReleases(issueKey: String) = TestUtil.client.getIssueReleases(issueKey)
}
