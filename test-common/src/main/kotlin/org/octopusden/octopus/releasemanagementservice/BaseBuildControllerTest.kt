package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDependencySearchRequest
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDependencySearchResult
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildParameters
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import java.util.stream.Stream

// A shared test base grows a function per scenario it covers, so the usual ceiling on
// class size does not carry its normal meaning here.
@Suppress("TooManyFunctions")
abstract class BaseBuildControllerTest : BaseReleaseManagementServiceTest {
    abstract fun getBuilds(
        component: String,
        params: Map<String, Any>,
    ): Collection<ShortBuildDTO>

    abstract fun getBuild(
        component: String,
        version: String,
    ): BuildDTO

    abstract fun getNotExistedBuildErrorResponse(
        component: String,
        version: String,
    ): ErrorResponse

    abstract fun searchBuildsByDependencies(request: BuildDependencySearchRequest): Collection<BuildDependencySearchResult>

    @ParameterizedTest
    @MethodSource("builds")
    fun getBuildsTest(
        params: Map<String, Any>,
        expectedBuilds: Collection<ShortBuildDTO>,
    ) {
        val builds = getBuilds("ReleaseManagementService", params)
        Assertions.assertEquals(expectedBuilds, builds)
    }

    @ParameterizedTest
    @MethodSource("build")
    fun getBuildTest(
        version: String,
        expected: BuildDTO,
    ) {
        Assertions.assertEquals(expected, getBuild("ReleaseManagementService", version))
    }

    @ParameterizedTest
    @MethodSource("buildLimitations")
    fun getBuildLimitationsTest(
        version: String,
        expectedLimitations: String?,
    ) {
        Assertions.assertEquals(expectedLimitations, getBuild("ReleaseManagementService", version).limitations)
    }

    @ParameterizedTest
    @MethodSource("buildParameters")
    fun getBuildParametersTest(
        version: String,
        expected: BuildParameters,
    ) {
        Assertions.assertEquals(expected, getBuild("ReleaseManagementService", version).buildParameters)
    }

    @ParameterizedTest
    @MethodSource("buildParameters")
    fun getBuildsParametersTest(
        version: String,
        expected: BuildParameters,
    ) {
        val build = getBuilds("ReleaseManagementService", mapOf("versions" to listOf(version))).single()
        Assertions.assertEquals(expected, build.buildParameters)
    }

    @Test
    fun getNotExistedBuildTest() {
        val errorResponse = getNotExistedBuildErrorResponse("ReleaseManagementService", "1.0.3")
        val expected = loadObject(
            "../test-data/releng/build-not-exist-error.json",
            object : TypeReference<ErrorResponse>() {},
        )
        Assertions.assertEquals(expected, errorResponse)
    }

    @Test
    fun searchBuildsByDependenciesTest() {
        val request = loadObject(
            "../test-data/releng/search-builds-by-dependencies-request.json",
            object : TypeReference<BuildDependencySearchRequest>() {},
        )
        val expected = loadObject(
            "../test-data/releng/search-builds-by-dependencies-response.json",
            object : TypeReference<Collection<BuildDependencySearchResult>>() {},
        )
        Assertions.assertEquals(expected, searchBuildsByDependencies(request))
    }

    private fun builds(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                mapOf("descending" to false, "limit" to 10),
                loadObject("../test-data/releng/builds.json", object : TypeReference<Collection<ShortBuildDTO>>() {}),
            ),
            Arguments.of(
                mapOf("limit" to 1),
                loadObject("../test-data/releng/builds-limit.json", object : TypeReference<Collection<ShortBuildDTO>>() {}),
            ),
            Arguments.of(
                mapOf("descending" to true),
                loadObject(
                    "../test-data/releng/builds-descending.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("minors" to listOf("2.0")),
                loadObject("../test-data/releng/builds-2.0.json", object : TypeReference<Collection<ShortBuildDTO>>() {}),
            ),
            Arguments.of(
                mapOf("statuses" to listOf("RELEASE")),
                loadObject(
                    "../test-data/releng/builds-release.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("versions" to listOf("1.0.1")),
                loadObject(
                    "../test-data/releng/builds_1.0.1.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("versions" to listOf("1.0.2")),
                loadObject(
                    "../test-data/releng/builds_1.0.2-hotfix.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("versions" to listOf("2.0.1")),
                loadObject(
                    "../test-data/releng/builds_2.0.1.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("branchNames" to listOf("release-.+")),
                loadObject(
                    "../test-data/releng/builds-with-branch-filter-1.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("branchNames" to listOf("release-1.0", "release-1.1")),
                loadObject(
                    "../test-data/releng/builds-with-branch-filter-2.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("branchNames" to listOf("not-existed-branch")),
                loadObject(
                    "../test-data/releng/branch-not-found.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("statuses" to listOf("BUILD"), "maxAgeBuilds" to 28),
                loadObject(
                    "../test-data/releng/builds-with-max-age-filter-1.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("statuses" to listOf("BUILD", "RC"), "maxAgeBuilds" to 28),
                loadObject(
                    "../test-data/releng/builds-with-max-age-filter-2.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("statuses" to listOf("BUILD"), "maxAgeBuilds" to 10),
                loadObject(
                    "../test-data/releng/builds-with-max-age-filter-3.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("javaVersions" to listOf("17")),
                loadObject("../test-data/releng/builds-java-17.json", object : TypeReference<Collection<ShortBuildDTO>>() {}),
            ),
            Arguments.of(
                mapOf("mavenVersions" to listOf("3.9")),
                loadObject("../test-data/releng/builds-java-17.json", object : TypeReference<Collection<ShortBuildDTO>>() {}),
            ),
            Arguments.of(
                mapOf("javaVersionPresent" to true),
                loadObject(
                    "../test-data/releng/builds-java-recorded.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
            Arguments.of(
                mapOf("javaVersionPresent" to false),
                loadObject(
                    "../test-data/releng/builds-java-not-recorded.json",
                    object : TypeReference<Collection<ShortBuildDTO>>() {},
                ),
            ),
        )

    private fun buildParameters(): Stream<Arguments> =
        Stream.of(
            Arguments.of("1.0.1", BuildParameters(javaVersion = "17", mavenVersion = "3.9")),
            Arguments.of("2.0.1", BuildParameters(javaVersion = "1.8")),
            Arguments.of("1.0.2", BuildParameters()),
        )

    // Referenced by name from @MethodSource, which static analysis cannot follow;
    // protected, like `build()` below, so it is not read as dead code.
    protected fun buildLimitations(): Stream<Arguments> =
        Stream.of(
            // limitations are provided by releng and have to survive the whole chain down to the client
            Arguments.of(
                "1.0.1",
                "Upgrade from 1.0.0 is not supported, reinstall required.\nSee TEST-1 for details.",
            ),
            // builds without limitations in the releng response must expose null, not a failure
            Arguments.of("2.0.1", null),
            Arguments.of("1.0.2", null),
        )

    protected fun build(): Stream<Arguments> =
        Stream.of(
            Arguments.of(
                "1.0.1",
                loadObject("../test-data/releng/build_1.0.1.json", object : TypeReference<BuildDTO>() {}),
            ),
            Arguments.of(
                "2.0.1",
                loadObject("../test-data/releng/build_2.0.1.json", object : TypeReference<BuildDTO>() {}),
            ),
            Arguments.of(
                "1.0.2",
                loadObject("../test-data/releng/build_1.0.2-hotfix.json", object : TypeReference<BuildDTO>() {}),
            ),
        )
}
