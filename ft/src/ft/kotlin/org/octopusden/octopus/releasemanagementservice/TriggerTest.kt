package org.octopusden.octopus.releasemanagementservice

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.teamcity.client.TeamcityClassicClient
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperties
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityTrigger
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityTriggers
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildStatus

class TriggerTest {
    @Test
    fun test() {
        val buildType = createBuildType(
            name = "Trigger Test",
            triggerSelector = """
                - component: ReleaseManagementService
                  status: RELEASE
            """.trimIndent()
        )
        Thread.sleep(DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL)
        with(readBuilds(buildType.id)) { //TODO: enhance TeamcityClient (support builds)
            Assertions.assertTrue(
                statusCode() / 100 == 2,
                "Unable to get builds of build type '${buildType.id}':\n${body()}"
            )
            Assertions.assertTrue(
                body().contains("\"count\":1,"),
                "Number of builds of build type '${buildType.id}' is not equals to 1:\n${body()}"
            )
        }
    }

    @Test
    fun testInReleaseBranch() {
        val buildType = createBuildType(
            name = "Trigger In Release Branch Test",
            triggerSelector = """
                - component: ReleaseManagementService
                  status: RELEASE
                  inReleaseBranch: true
            """.trimIndent()
        )
        Thread.sleep(DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL)
        with(readBuilds(buildType.id)) { //TODO: enhance TeamcityClient (support builds)
            Assertions.assertTrue(
                statusCode() / 100 == 2,
                "Unable to get builds of build type '${buildType.id}':\n${body()}"
            )
            Assertions.assertTrue(
                body().contains("\"count\":1,"),
                "Number of builds of build type '${buildType.id}' is not equals to 1:\n${body()}"
            )
        }
    }

    @Test
    fun testQuietPeriod() {
        val releaseTime = TestUtil.client.getBuild("ReleaseManagementService", "2.0.1").statusHistory.getValue(BuildStatus.RELEASE)
        val diffSec = (System.currentTimeMillis() - releaseTime.time) / 1000
        val pollIntervalSec = DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL / 1000
        val dynamicQuietPeriod = diffSec + pollIntervalSec
        val buildType = createBuildType(
            name = "Test quiet period",
            triggerSelector = """
            - component: ReleaseManagementService
              status: RELEASE
        """.trimIndent(),
            quietPeriod = dynamicQuietPeriod.toString()
        )
        Thread.sleep(DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL)
        with(readBuilds(buildType.id)) {
            Assertions.assertTrue(
                statusCode() / 100 == 2,
                "Unable to get builds of build type '${buildType.id}':\n${body()}"
            )
            Assertions.assertTrue(
                body().contains("\"count\":0,"),
                "Expected 0 builds during quiet period, but got:\n${body()}"
            )
        }
        Thread.sleep(DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL)
        with(readBuilds(buildType.id)) {
            Assertions.assertTrue(
                statusCode() / 100 == 2,
                "Unable to get builds of build type '${buildType.id}':\n${body()}"
            )
            Assertions.assertTrue(
                body().contains("\"count\":1,"),
                "Expected 1 build after quiet period elapsed, but got:\n${body()}"
            )
        }
    }

    private fun createBuildType(name: String, triggerSelector: String, quietPeriod: String = "0"): TeamcityBuildType {
        return teamcityClient.createBuildType(
            TeamcityCreateBuildType(
                name = name,
                project = TeamcityLinkProject("RDDepartment"),
                triggers = TeamcityTriggers(
                    triggers = listOf(
                        TeamcityTrigger(
                            "release-management-teamcity-build-trigger", false, TeamcityProperties(
                                properties = listOf(
                                    TeamcityProperty(
                                        "release.management.build.trigger.service.url",
                                        releaseManagementServiceUrl
                                    ),
                                    TeamcityProperty("release.management.build.trigger.selections", triggerSelector),
                                    TeamcityProperty("release.management.build.trigger.quiet.period", quietPeriod)
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    private fun readBuilds(buildTypeId: String): HttpResponse<String> {
        return httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI("${teamcityApiUrl}/builds?locator=buildType:(id:${buildTypeId})"))
                .header("Origin", teamcityUrl)
                .header("Authorization", TEAMCITY_AUTHORIZATION)
                .header("Accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
    }

    companion object {
        private val hostTeamcity2022 = System.getProperty("test.teamcity-2022-host")
            ?: throw Exception("System property 'test.teamcity-2022-host' must be defined")
        private val hostReleaseManagement = System.getProperty("test.release-management-host-for-teamcity")
            ?: throw Exception("System property 'test.release-management-host-for-teamcity' must be defined")
        val releaseManagementServiceUrl = "http://$hostReleaseManagement"
        val teamcityUrl = "http://$hostTeamcity2022"
        val teamcityApiUrl = "$teamcityUrl/app/rest/2018.1"
        const val DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL = 60000L
        const val TEAMCITY_AUTHORIZATION = "Basic YWRtaW46YWRtaW4="

        private val teamcityClient = TeamcityClassicClient(object : ClientParametersProvider {
            override fun getApiUrl() = teamcityUrl
            override fun getAuth() = StandardBasicCredCredentialProvider("admin", "admin")
        })

        private val httpClient = HttpClient.newHttpClient()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            with( //TODO: enhance TeamcityClient (support agents)
                httpClient.send(
                    HttpRequest.newBuilder()
                        .uri(URI("${teamcityApiUrl}/agents/name:test-agent/authorized"))
                        .header("Origin", teamcityUrl)
                        .header("Authorization", TEAMCITY_AUTHORIZATION)
                        .header("Content-Type", "text/plain")
                        .method("PUT", HttpRequest.BodyPublishers.ofString("true"))
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                )
            ) {
                if (statusCode() / 100 != 2) {
                    throw RuntimeException("Unable to authorize 'test-agent':\n${body()}")
                }
            }
        }
    }
}