package org.octopusden.task

import com.google.common.io.Files
import com.google.common.net.HttpHeaders
import org.apache.http.entity.ContentType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import java.io.File
import java.nio.charset.Charset


open class MigrateMockData : DefaultTask() {

    private val mockServerClient = MockServerClient("localhost", 1080)

    @get:Input
    lateinit var testDataDir: String

    @TaskAction
    fun startMockServer() {
        mockServerClient.reset()
        endpointToResponseFileName.forEach {
            generateMockserverData(it.key.first, it.key.second, testDataDir + File.separator + it.value, 200)
        }
        endpointNotFoundToResponseFileName.forEach {
            generateMockserverData(it.key.first, it.key.second, testDataDir + File.separator + it.value, 404)
        }
    }

    private fun generateMockserverData(endpoint: String, params: Map<String, String>, filename: String, status: Int) {
        val body = Files.asCharSource(File(filename), Charset.defaultCharset()).read()
        val request = HttpRequest.request()
            .withMethod("GET")
            .withPath(endpoint)
        params.forEach {
            request.withQueryStringParameter(it.key, it.value)
        }
        mockServerClient.`when`(request)
            .respond {
                logger.debug(
                    "MockServer request: ${it.method} ${it.path} ${it.queryStringParameterList.joinToString(",")} ${
                        it.pathParameterList.joinToString(
                            ","
                        )
                    }"
                )
                HttpResponse.response()
                    .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                    .withBody(body)
                    .withStatusCode(status)
            }
    }

    companion object {
        private val endpointToResponseFileName = mapOf(
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to emptyMap<String, String>() to "releng/builds.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/version/1.0.1/build" to emptyMap<String, String>() to "releng/build_1.0.1.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/version/1.0.2/build" to emptyMap<String, String>() to "releng/build_1.0.2.json",
        )
        private val endpointNotFoundToResponseFileName = mapOf(
            "/rest/release-engineering/3/component/ReleaseManagementService/version/1.0.3/build" to emptyMap<String, String>() to "releng/build-not-exist-error.json"
        )
    }
}
