package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse

abstract class BaseSupportControllerTest : BaseReleaseManagementServiceTest {

    abstract fun getComponents(): Collection<ComponentDTO>
    abstract fun getComponent(component: String): ComponentDTO
    abstract fun getNotExistedComponentErrorResponse(component: String): ErrorResponse
    abstract fun getMandatoryUpdateComponents(component: String, version: String): Collection<ComponentDTO>

    @Test
    fun getComponentsTest() {
        val expectedComponents =
            loadObject("../test-data/releng/components.json", object : TypeReference<Collection<ComponentDTO>>() {})
        val components = getComponents()
        Assertions.assertEquals(expectedComponents, components)
    }

    @ParameterizedTest
    @MethodSource("component")
    fun getComponentTest(component: String, expectedComponent: ComponentDTO) {
        Assertions.assertEquals(expectedComponent, getComponent(component))
    }

    @Test
    fun getNotExistedComponentTest() {
        val errorResponse = getNotExistedComponentErrorResponse("NotExistedInDB")
        val expected = loadObject(
            "../test-data/releng/component-not-exist-error.json",
            object : TypeReference<ErrorResponse>() {})
        Assertions.assertEquals(expected, errorResponse)
    }

    @Test
    fun getMandatoryUpdateComponentsTest() {
        val expectedComponents = loadObject("../test-data/releng/mandatory-component.json", object : TypeReference<Collection<ComponentDTO>>() {})
        val dependencyComponent = loadObject("../test-data/releng/build_2.0.1.json", object : TypeReference<BuildDTO>() {}).dependencies.first()
        val components = getMandatoryUpdateComponents(
            dependencyComponent.component,
            dependencyComponent.version.replace(Regex("(\\d+)$")) {(it.value.toInt() + 1).toString()}
        )
        Assertions.assertEquals(expectedComponents, components)
    }

    private fun component(): Stream<Arguments> = Stream.of(
        Arguments.of(
            "ReleaseManagementService",
            loadObject("../test-data/releng/component_rm_service.json", object : TypeReference<ComponentDTO>() {})
        ),
        Arguments.of(
            "LegacyReleaseManagementService",
            loadObject(
                "../test-data/releng/component_legacy_rm_service.json", object : TypeReference<ComponentDTO>() {})
        )
    )
}