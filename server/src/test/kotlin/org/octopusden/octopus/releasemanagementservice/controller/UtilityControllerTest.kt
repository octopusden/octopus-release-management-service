package org.octopusden.octopus.releasemanagementservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.octopusden.octopus.releasemanagementservice.BaseUtilityControllerTest
import org.octopusden.octopus.releasemanagementservice.ReleaseManagementServiceApplication
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.service.impl.UtilityServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc


@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [ReleaseManagementServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class UtilityControllerTest: BaseUtilityControllerTest(), BaseControllerTest {

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var mapper: ObjectMapper
    override fun getMockMvc(): MockMvc = mvc
    override fun getObjectMapper(): ObjectMapper = mapper

    @SpyBean
    lateinit var utilityService: UtilityServiceImpl

    @BeforeEach
    fun setupStubs() {
        stubCreateMandatoryUpdate()
    }

    override fun createMandatoryUpdate(dryRun: Boolean, dto: MandatoryUpdateDTO): MandatoryUpdateResponseDTO {
        return post(
            200,
            object: TypeReference<MandatoryUpdateResponseDTO>() {},
            "/rest/api/1/utils/mandatory-update",
            mapOf("dryRun" to dryRun),
            dto
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun stubCreateMandatoryUpdate() {
        val cases = mandatoryUpdateCases().map { it.get() }.toList()

        cases.forEach { args ->
            val dryRun = args[0] as Boolean
            val component = args[1] as String
            val version = args[2] as String
            val startVersion = args[3] as String
            val excludeVersions = args[4] as Set<String>
            val excludeComponents = args[5] as Set<String>
            val excludeSystems = args[6] as Set<String>
            val expected = args[7] as MandatoryUpdateResponseDTO

            val filter = MandatoryUpdateFilterDTO(
                activeLinePeriod = 180,
                startVersion = startVersion,
                excludeVersions = excludeVersions,
                excludeComponents = excludeComponents,
                excludeSystems = excludeSystems
            )

            doReturn(expected).whenever(utilityService)
                .createMandatoryUpdate(
                    eq(dryRun),
                    argThat { dto ->
                        dto.component == component &&
                                dto.version == version &&
                                dto.filter == filter
                    }
                )
        }
    }
}