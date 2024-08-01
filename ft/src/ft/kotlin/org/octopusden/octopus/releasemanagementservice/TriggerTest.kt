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
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperties
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityTrigger
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityTriggers

class TriggerTest {
    @Test
    fun test() {
        val buildType = teamcityClient.createBuildType(
            TeamcityCreateBuildType(
                name = "Trigger Test",
                project = TeamcityLinkProject("RDDepartment"),
                triggers = TeamcityTriggers(
                    triggers = listOf(
                        TeamcityTrigger(
                            "release-management-teamcity-build-trigger", false, TeamcityProperties(
                                properties = listOf(
                                    TeamcityProperty(
                                        "release.management.build.trigger.service.url",
                                        "http://release-management-service:8080"
                                    ),
                                    TeamcityProperty(
                                        "release.management.build.trigger.selections", """
                                    - component: ReleaseManagementService
                                      status: RELEASE
                                """.trimIndent()
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        Thread.sleep(60000L) //3 * <default_teamcity_trigger_polling_interval>
        with( //TODO: enhance TeamcityClient (support builds)
            httpClient.send(
                HttpRequest.newBuilder()
                    .uri(URI("http://localhost:8111/app/rest/2018.1/builds?locator=buildType:(id:${buildType.id})"))
                    .header("Origin", "http://localhost:8111")
                    .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                    .header("Accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            )
        ) {
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

    companion object {
        private val teamcityClient = TeamcityClassicClient(object : ClientParametersProvider {
            override fun getApiUrl() = "http://localhost:8111"

            override fun getAuth() = StandardBasicCredCredentialProvider("admin", "admin")
        })

        private val httpClient = HttpClient.newHttpClient()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            with( //TODO: enhance TeamcityClient (support agents)
                httpClient.send(
                    HttpRequest.newBuilder()
                        .uri(URI("http://localhost:8111/app/rest/2018.1/agents/name:test-agent/authorized"))
                        .header("Origin", "http://localhost:8111")
                        .header("Authorization", "Basic YWRtaW46YWRtaW4=")
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