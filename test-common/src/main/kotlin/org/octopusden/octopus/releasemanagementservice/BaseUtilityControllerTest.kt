package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

abstract class BaseUtilityControllerTest: BaseReleaseManagementServiceTest {

    abstract fun createMandatoryUpdate(dryRun: Boolean, dto: MandatoryUpdateDTO): MandatoryUpdateResponseDTO

    @ParameterizedTest
    @MethodSource("mandatoryUpdateCases")
    fun createMandatoryUpdateTest(dryRun: Boolean, excludeComponents: Set<String>, excludeSystems: Set<String>, expected: MandatoryUpdateResponseDTO) {
        val result = createMandatoryUpdate(
            dryRun,
            MandatoryUpdateDTO(
                component = "dependency-component-first",
                version = "1.0.2",
                projectKey = "PROJ",
                epicName = "Mandatory update to dependency-component-first",
                dueDate = Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)),
                notice = "Something",
                customer = "Octopus",
                filter = MandatoryUpdateFilterDTO(
                    activeLinePeriod = 180,
                    excludeComponents = excludeComponents,
                    excludeSystems = excludeSystems
                )
            )
        )
        Assertions.assertEquals(expected, result)
    }

    private fun mandatoryUpdateCases(): Stream<Arguments> = Stream.of(
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