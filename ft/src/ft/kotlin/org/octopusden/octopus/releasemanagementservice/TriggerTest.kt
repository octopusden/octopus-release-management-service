package org.octopusden.octopus.releasemanagementservice

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TriggerTest {
    @Test
    fun test() {
        //TODO
    }

    companion object {
        private val httpClient = HttpClient.newHttpClient()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            with(
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