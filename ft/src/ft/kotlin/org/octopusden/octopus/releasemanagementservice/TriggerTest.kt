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

    private fun createBuildType(name: String, triggerSelector: String): TeamcityBuildType {
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
                                        RELEASE_MANAGEMENT_SERVICE_URL
                                    ),
                                    TeamcityProperty("release.management.build.trigger.selections", triggerSelector)
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
                .uri(URI("${TEAMCITY_API_URL}/builds?locator=buildType:(id:${buildTypeId})"))
                .header("Origin", TEAMCITY_URL)
                .header("Authorization", TEAMCITY_AUTHORIZATION)
                .header("Accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
    }

    companion object {
        const val DEFAULT_TEAMCITY_TRIGGER_POLLING_INTERVAL = 60000L
        const val RELEASE_MANAGEMENT_SERVICE_URL = "http://release-management-service:8080"
        const val TEAMCITY_URL = "http://localhost:8111"
        const val TEAMCITY_API_URL = "${TEAMCITY_URL}/app/rest/2018.1"
        const val TEAMCITY_AUTHORIZATION = "Basic YWRtaW46YWRtaW4="

        private val teamcityClient = TeamcityClassicClient(object : ClientParametersProvider {
            override fun getApiUrl() = TEAMCITY_URL
            override fun getAuth() = StandardBasicCredCredentialProvider("admin", "admin")
        })

        private val httpClient = HttpClient.newHttpClient()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            with( //TODO: enhance TeamcityClient (support agents)
                httpClient.send(
                    HttpRequest.newBuilder()
                        .uri(URI("${TEAMCITY_API_URL}/agents/name:test-agent/authorized"))
                        .header("Origin", TEAMCITY_URL)
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