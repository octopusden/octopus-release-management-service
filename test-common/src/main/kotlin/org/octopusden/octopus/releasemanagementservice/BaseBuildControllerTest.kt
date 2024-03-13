package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import java.util.stream.Stream

abstract class BaseBuildControllerTest : BaseReleaseManagementServiceTest {

    abstract fun getBuilds(component: String): Collection<ShortBuildDTO>
    abstract fun getBuild(component: String, version: String): BuildDTO
    abstract fun getNotExistedBuildErrorResponse(component: String, version: String): ErrorResponse

    @Test
    fun getBuildsTest() {
        val builds = getBuilds("ReleaseManagementService")
        Assertions.assertEquals(
            loadObject("../test-data/releng/builds.json",
                object : TypeReference<Collection<ShortBuildDTO>>() {}), builds
        )
    }

    @ParameterizedTest
    @MethodSource("builds")
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

    private fun builds(): Stream<Arguments> = Stream.of(
        Arguments.of(
            "1.0.1", loadObject("../test-data/releng/build_1.0.1.json", object : TypeReference<BuildDTO>() {}),
            "1.0.2", loadObject("../test-data/releng/build_1.0.2.json", object : TypeReference<BuildDTO>() {})
        )
    )
}