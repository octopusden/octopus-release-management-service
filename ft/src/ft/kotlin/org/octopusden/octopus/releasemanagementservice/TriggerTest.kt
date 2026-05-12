package org.octopusden.octopus.releasemanagementservice

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
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class TriggerTest {
    @Test
    fun test() {
        val buildType =
            createBuildType(
                name = "Trigger Test",
                triggerSelector =
                    """
                    - component: ReleaseManagementService
                      status: RELEASE
                    """.trimIndent(),
            )
        // TD-002: switch to TeamcityClient builds API once supported (see docs/TECH_DEBT.md).
        pollBuildsUntilCount(buildType.id, expectedCount = 1)
    }

    @Test
    fun testInReleaseBranch() {
        val buildType =
            createBuildType(
                name = "Trigger In Release Branch Test",
                triggerSelector =
                    """
                    - component: ReleaseManagementService
                      status: RELEASE
                      inReleaseBranch: true
                    """.trimIndent(),
            )
        // TD-002: switch to TeamcityClient builds API once supported (see docs/TECH_DEBT.md).
        pollBuildsUntilCount(buildType.id, expectedCount = 1)
    }

    @Test
    fun testQuietPeriod() {
        val releaseTime =
            TestUtil.client
                .getBuild("ReleaseManagementService", "2.0.1")
                .statusHistory
                .getValue(BuildStatus.RELEASE)
        val diffSec = (System.currentTimeMillis() - releaseTime.time) / 1000
        val pollIntervalSec = DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL / 1000
        val dynamicQuietPeriod = diffSec + pollIntervalSec
        val buildType =
            createBuildType(
                name = "Test quiet period",
                triggerSelector =
                    """
                    - component: ReleaseManagementService
                      status: RELEASE
                    """.trimIndent(),
                quietPeriod = dynamicQuietPeriod.toString(),
            )
        Thread.sleep(DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL)
        with(readBuildsWithRetry(buildType.id)) {
            Assertions.assertTrue(
                statusCode() / 100 == 2,
                "Unable to get builds of build type '${buildType.id}':\n${body()}",
            )
            Assertions.assertTrue(
                body().contains("\"count\":0,"),
                "Expected 0 builds during quiet period, but got:\n${body()}",
            )
        }
        pollBuildsUntilCount(buildType.id, expectedCount = 1, failureMessage = "Expected 1 build after quiet period elapsed")
    }

    private fun pollBuildsUntilCount(
        buildTypeId: String,
        expectedCount: Int,
        failureMessage: String? = null,
    ) {
        val deadline = System.currentTimeMillis() + BUILDS_POLL_TIMEOUT
        var lastBody = ""
        while (System.currentTimeMillis() < deadline) {
            val response = readBuilds(buildTypeId)
            lastBody = "status=${response.statusCode()}\n${response.body()}"
            if (response.statusCode() / 100 == 5) {
                Thread.sleep(BUILDS_POLL_INTERVAL)
                continue
            }
            Assertions.assertTrue(
                response.statusCode() / 100 == 2,
                "Unable to get builds of build type '$buildTypeId': $lastBody",
            )
            lastBody = response.body()
            if (lastBody.contains("\"count\":$expectedCount,")) {
                return
            }
            Thread.sleep(BUILDS_POLL_INTERVAL)
        }
        Assertions.fail<Unit>(
            "${failureMessage ?: "Number of builds of build type '$buildTypeId' is not equals to $expectedCount"}:\n$lastBody",
        )
    }

    private fun createBuildType(
        name: String,
        triggerSelector: String,
        quietPeriod: String = "0",
    ): TeamcityBuildType =
        teamcityClient.createBuildType(
            TeamcityCreateBuildType(
                name = name,
                project = TeamcityLinkProject("RDDepartment"),
                triggers =
                    TeamcityTriggers(
                        triggers =
                            listOf(
                                TeamcityTrigger(
                                    "release-management-teamcity-build-trigger",
                                    false,
                                    TeamcityProperties(
                                        properties =
                                            listOf(
                                                TeamcityProperty(
                                                    "release.management.build.trigger.service.url",
                                                    releaseManagementServiceUrl,
                                                ),
                                                TeamcityProperty("release.management.build.trigger.selections", triggerSelector),
                                                TeamcityProperty("release.management.build.trigger.quiet.period", quietPeriod),
                                            ),
                                    ),
                                ),
                            ),
                    ),
            ),
        )

    private fun readBuildsWithRetry(buildTypeId: String): HttpResponse<String> {
        for (attempt in 0 until RETRY_ATTEMPTS) {
            val response = readBuilds(buildTypeId)
            if (response.statusCode() / 100 != 5) {
                return response
            }
            if (attempt < RETRY_ATTEMPTS - 1) {
                Thread.sleep(RETRY_DELAYS[attempt])
            }
        }
        return readBuilds(buildTypeId)
    }

    private fun readBuilds(buildTypeId: String): HttpResponse<String> =
        httpClient.send(
            HttpRequest
                .newBuilder()
                .uri(URI("$teamcityApiUrl/builds?locator=buildType:(id:$buildTypeId)"))
                .header("Origin", teamcityUrl)
                .header("Authorization", TEAMCITY_AUTHORIZATION)
                .header("Accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    companion object {
        private val hostTeamcity2022 =
            System.getProperty("test.teamcity-2022-host")
                ?: throw Exception("System property 'test.teamcity-2022-host' must be defined")
        private val hostReleaseManagement =
            System.getProperty("test.release-management-host-for-teamcity")
                ?: throw Exception("System property 'test.release-management-host-for-teamcity' must be defined")
        val releaseManagementServiceUrl = "http://$hostReleaseManagement"
        val teamcityUrl = "http://$hostTeamcity2022"
        val teamcityApiUrl = "$teamcityUrl/app/rest/2018.1"
        const val DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL = 60000L
        const val TEAMCITY_AUTHORIZATION = "Basic YWRtaW46YWRtaW4="
        private const val READINESS_POLL_INTERVAL = 5000L
        private const val READINESS_TIMEOUT = 180000L
        private const val BUILDS_POLL_INTERVAL = 5000L
        private const val BUILDS_POLL_TIMEOUT = 180000L
        private const val RETRY_ATTEMPTS = 5
        private val RETRY_DELAYS = longArrayOf(1000, 2000, 4000, 8000, 16000)

        private val teamcityClient =
            TeamcityClassicClient(
                object : ClientParametersProvider {
                    override fun getApiUrl() = teamcityUrl

                    override fun getAuth() = StandardBasicCredCredentialProvider("admin", "admin")
                },
            )

        private val httpClient =
            HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()

        private fun waitForTeamcityReady() {
            val deadline = System.currentTimeMillis() + READINESS_TIMEOUT
            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI("$teamcityUrl/app/rest/server/version"))
                    .header("Origin", teamcityUrl)
                    .header("Authorization", TEAMCITY_AUTHORIZATION)
                    .header("Accept", "text/plain")
                    .timeout(Duration.ofSeconds(30))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build()
            while (System.currentTimeMillis() < deadline) {
                try {
                    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() / 100 == 2) {
                        return
                    }
                } catch (_: IOException) {
                }
                Thread.sleep(READINESS_POLL_INTERVAL)
            }
            throw RuntimeException(
                "TeamCity REST API at $teamcityUrl did not become ready within ${READINESS_TIMEOUT / 1000}s",
            )
        }

        private fun sendWithRetry(request: HttpRequest): HttpResponse<String> {
            var lastException: Exception? = null
            var lastResponse: HttpResponse<String>? = null
            for (attempt in 0 until RETRY_ATTEMPTS) {
                try {
                    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() / 100 == 2) {
                        return response
                    }
                    lastResponse = response
                    if (response.statusCode() / 100 != 5) {
                        return response
                    }
                } catch (e: IOException) {
                    lastException = e
                }
                if (attempt < RETRY_ATTEMPTS - 1) {
                    Thread.sleep(RETRY_DELAYS[attempt])
                }
            }
            if (lastResponse != null) {
                return lastResponse
            }
            throw RuntimeException(
                "Request to ${request.uri()} failed after $RETRY_ATTEMPTS attempts",
                lastException,
            )
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            waitForTeamcityReady()
            // TD-003: switch to TeamcityClient agents API once supported (see docs/TECH_DEBT.md).
            with(
                sendWithRetry(
                    HttpRequest
                        .newBuilder()
                        .uri(URI("$teamcityApiUrl/agents/name:test-agent/authorized"))
                        .header("Origin", teamcityUrl)
                        .header("Authorization", TEAMCITY_AUTHORIZATION)
                        .header("Content-Type", "text/plain")
                        .timeout(Duration.ofSeconds(30))
                        .method("PUT", HttpRequest.BodyPublishers.ofString("true"))
                        .build(),
                ),
            ) {
                if (statusCode() / 100 != 2) {
                    throw RuntimeException(
                        "Unable to authorize 'test-agent': status=${statusCode()}\n${body()}",
                    )
                }
            }
        }
    }
}
