package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import java.util.Date
import java.util.concurrent.TimeUnit

abstract class BaseBuildControllerTest : BaseReleaseManagementServiceTest {

    abstract fun getBuilds(component: String, params: Map<String, Any>): Collection<ShortBuildDTO>
    abstract fun getBuild(component: String, version: String): BuildDTO
    abstract fun getNotExistedBuildErrorResponse(component: String, version: String): ErrorResponse
    abstract fun createMandatoryUpdate(component: String,version: String, dryRun: Boolean, dto: MandatoryUpdateDTO): MandatoryUpdateResponseDTO

    @ParameterizedTest
    @MethodSource("builds")
    fun getBuildsTest(params: Map<String, Any>, expectedBuilds: Collection<ShortBuildDTO>) {
        val builds = getBuilds("ReleaseManagementService", params)
        Assertions.assertEquals(expectedBuilds, builds)
    }

    @ParameterizedTest
    @MethodSource("build")
    fun getBuildTest(version: String, expected: BuildDTO) {
        Assertions.assertEquals(expected, getBuild("ReleaseManagementService", version))
    }

    @Test
    fun getNotExistedBuildTest() {
        val errorResponse = getNotExistedBuildErrorResponse("ReleaseManagementService", "1.0.3")
        val expected = loadObject(
            "../test-data/releng/build-not-exist-error.json",
            object : TypeReference<ErrorResponse>() {})
        Assertions.assertEquals(expected, errorResponse)
    }

    @ParameterizedTest
    @MethodSource("mandatoryUpdate")
    fun createMandatoryUpdateTest(dryRun: Boolean, excludeComponents: Set<String>, systems: Set<String>, expected: MandatoryUpdateResponseDTO) {
        val result = createMandatoryUpdate(
            "dependency-component-first",
            "1.0.2",
            dryRun,
            MandatoryUpdateDTO(
                projectKey = "PROJ",
                epicName = "Mandatory update to dependency-component-first",
                dueDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)),
                notice = "Something",
                customer = "Octopus",
                filter = MandatoryUpdateFilterDTO(
                    activeLinePeriod = 180,
                    excludeComponents = excludeComponents,
                    systems = systems
                )
            )
        )
        Assertions.assertEquals(expected, result)
    }

    private fun builds(): Stream<Arguments> = Stream.of(
        Arguments.of(
            mapOf("descending" to false, "limit" to 10),
            loadObject("../test-data/releng/builds.json", object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("limit" to 1),
            loadObject("../test-data/releng/builds-limit.json", object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("descending" to true),
            loadObject(
                "../test-data/releng/builds-descending.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("minors" to listOf("2.0")),
            loadObject("../test-data/releng/builds-2.0.json", object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("statuses" to listOf("RELEASE")),
            loadObject(
                "../test-data/releng/builds-release.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("versions" to listOf("1.0.1")),
            loadObject(
                "../test-data/releng/builds_1.0.1.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("versions" to listOf("2.0.1")),
            loadObject(
                "../test-data/releng/builds_2.0.1.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("branchNames" to listOf("release-.+")),
            loadObject(
                "../test-data/releng/builds-with-branch-filter-1.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("branchNames" to listOf("release-1.0", "release-1.1")),
            loadObject(
                "../test-data/releng/builds-with-branch-filter-2.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(
            mapOf("branchNames" to listOf("not-existed-branch")),
            loadObject(
                "../test-data/releng/branch-not-found.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(mapOf("statuses" to listOf("BUILD"), "maxAgeBuilds" to 28),
            loadObject(
                "../test-data/releng/builds-with-max-age-filter-1.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(mapOf("statuses" to listOf("BUILD", "RC"), "maxAgeBuilds" to 28),
            loadObject(
                "../test-data/releng/builds-with-max-age-filter-2.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        ),
        Arguments.of(mapOf("statuses" to listOf("BUILD"), "maxAgeBuilds" to 10),
            loadObject(
                "../test-data/releng/builds-with-max-age-filter-3.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {})
        )
    )

    private fun build(): Stream<Arguments> = Stream.of(
        Arguments.of(
            "1.0.1",
            loadObject("../test-data/releng/build_1.0.1.json", object : TypeReference<BuildDTO>() {})
        ),
        Arguments.of(
            "2.0.1",
            loadObject("../test-data/releng/build_2.0.1.json", object : TypeReference<BuildDTO>() {})
        )
    )

    private fun mandatoryUpdate(): Stream<Arguments> = Stream.of(
        Arguments.of(
            "true", emptySet<String>(), emptySet<String>(),
            loadObject("../test-data/releng/create-mandatory-update-1.json", object : TypeReference<MandatoryUpdateResponseDTO>() {})
        ),
        Arguments.of(
            "true", setOf("main-component-second"), setOf("CLASSIC"),
            loadObject("../test-data/releng/create-mandatory-update-2.json", object : TypeReference<MandatoryUpdateResponseDTO>() {})
        ),
        Arguments.of(
            "false", emptySet<String>(), emptySet<String>(),
            loadObject("../test-data/releng/create-mandatory-update-3.json", object : TypeReference<MandatoryUpdateResponseDTO>() {})
        )
    )
}