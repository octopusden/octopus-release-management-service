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

    private fun generateMockserverData(endpoint: String, params: Map<String, List<String>>, filename: String, status: Int) {
        val body = Files.asCharSource(File(filename), Charset.defaultCharset()).read()
        val request = HttpRequest.request()
            .withMethod("GET")
            .withPath(endpoint)
        params.forEach { (key, values) ->
            request.withQueryStringParameter(key, *values.toTypedArray())
        }
        mockServerClient.`when`(request)
            .respond {
                logger.debug(
                    "MockServer request: {} {} {} {}",
                    it.method,
                    it.path,
                    it.queryStringParameterList.joinToString(","),
                    it.pathParameterList.joinToString(
                        ","
                    )
                )
                HttpResponse.response()
                    .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                    .withBody(body)
                    .withStatusCode(status)
            }
    }

    companion object {
        private val defaultParams = mapOf("descending" to listOf("false"))
        private val endpointToResponseFileName = mapOf(
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("limit" to listOf("10"))to "releng/builds.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("limit" to listOf("1")) to "releng/builds-limit.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("descending" to listOf("true")) to "releng/builds-descending.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("minors" to listOf("2.0")) to "releng/builds-2.0.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("statuses" to listOf("RELEASE")) to "releng/builds-release.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("versions" to listOf("1.0.1")) to "releng/builds_1.0.1.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("versions" to listOf("2.0.1")) to "releng/builds_2.0.1.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("branchNames" to listOf("release-1.0", "release-1.1")) to "releng/builds-with-branch-filter-2.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("branchNames" to listOf("release-\\.\\+")) to "releng/builds-with-branch-filter-1.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("branchNames" to listOf("not-existed-branch")) to "releng/branch-not-found.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("statuses" to listOf("BUILD", "RC"), "maxAgeBuilds" to listOf("28")) to "releng/builds-with-max-age-filter-2.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("statuses" to listOf("BUILD"), "maxAgeBuilds" to listOf("28")) to "releng/builds-with-max-age-filter-1.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/builds" to defaultParams + mapOf("statuses" to listOf("BUILD"), "maxAgeBuilds" to listOf("10")) to "releng/builds-with-max-age-filter-3.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/version/1.0.1/build" to emptyMap<String, List<String>>() to "releng/build_1.0.1.json",
            "/rest/release-engineering/3/component/ReleaseManagementService/version/2.0.1/build" to emptyMap<String, List<String>>() to "releng/build_2.0.1.json",
            "/rest/release-engineering/3/component-management" to emptyMap<String, List<String>>() to "releng/components.json",
            "/rest/release-engineering/3/component-management/ReleaseManagementService" to emptyMap<String, List<String>>() to "releng/component_rm_service.json",
            "/rest/release-engineering/3/component-management/LegacyReleaseManagementService" to emptyMap<String, List<String>>() to "releng/component_legacy_rm_service.json",
        )
        private val endpointNotFoundToResponseFileName = mapOf(
            "/rest/release-engineering/3/component/ReleaseManagementService/version/1.0.3/build" to emptyMap<String, List<String>>() to "releng/build-not-exist-error.json",
            "/rest/release-engineering/3/component-management/NotExistedInDB" to emptyMap<String, List<String>>() to "releng/component-not-exist-error.json"
        )
    }
}
