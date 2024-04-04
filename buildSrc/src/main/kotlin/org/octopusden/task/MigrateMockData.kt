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
import org.mockserver.model.HttpStatusCode
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
            generateMockserverData(it.key.first, it.key.second, testDataDir + File.separator + it.value, HttpStatusCode.OK_200.code())
        }
        endpointNotFoundToResponseFileName.forEach {
            generateMockserverData(it.key.first, it.key.second, testDataDir + File.separator + it.value, HttpStatusCode.NOT_FOUND_404.code())
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
        private val defaultParams = mapOf("descending" to "false")
        private val endpointToResponseFileName = mapOf(
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("limit" to "10")to "releng/builds.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("limit" to "1") to "releng/builds-limit.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("descending" to "true") to "releng/builds-descending.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("minors" to "2.0") to "releng/builds-2.0.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("statuses" to "RELEASE") to "releng/builds-release.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/version/1.0.1/build" to emptyMap<String, String>() to "releng/build_1.0.1.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/version/2.0.1/build" to emptyMap<String, String>() to "releng/build_2.0.1.json",
        )
        private val endpointNotFoundToResponseFileName = mapOf(
            "/rest/release-engineering/3/component/ReleaseManagementService/version/1.0.3/build" to emptyMap<String, String>() to "releng/build-not-exist-error.json"
        )
    }
}
