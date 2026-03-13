package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.octopus.releasemanagementservice.client.common.dto.IssueReleasesDTO
import java.util.stream.Stream

abstract class BaseIssueControllerTest : BaseReleaseManagementServiceTest {
    abstract fun getIssueReleases(issueKey: String): IssueReleasesDTO

    @ParameterizedTest
    @MethodSource("issueReleasesCases")
    fun getIssueReleasesTest(
        issueKey: String,
        expected: IssueReleasesDTO,
    ) {
        Assertions.assertEquals(expected, getIssueReleases(issueKey))
    }

    fun issueReleasesCases(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                "TEST-1",
                loadObject("../test-data/releng/issue-releases.json", object : TypeReference<IssueReleasesDTO>() {}),
            ),
        )
}
