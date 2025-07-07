package org.octopusden.octopus.releasemanagementservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.octopusden.octopus.releasemanagementservice.BaseBuildControllerTest
import org.octopusden.octopus.releasemanagementservice.ReleaseManagementServiceApplication
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import org.octopusden.octopus.releasemanagementservice.service.impl.BuildServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
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
class BuildControllerTest : BaseBuildControllerTest(), BaseControllerTest {

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var mapper: ObjectMapper
    override fun getMockMvc(): MockMvc = mvc
    override fun getObjectMapper(): ObjectMapper = mapper

    @SpyBean
    lateinit var buildService: BuildServiceImpl

    @BeforeEach
    fun setupStubs() {
        stubCreateMandatoryUpdate()
    }

    override fun getBuilds(component: String, params: Map<String, Any>): Collection<ShortBuildDTO> {
        return get(
            HttpStatus.OK.value(),
            object : TypeReference<Collection<ShortBuildDTO>>() {},
            "/rest/api/1/builds/component/{component}",
            params,
            component
        )
    }

    override fun getBuild(component: String, version: String): BuildDTO {
        return get(
            HttpStatus.OK.value(),
            object : TypeReference<BuildDTO>() {},
            "/rest/api/1/builds/component/{component}/version/{version}",
            emptyMap(),
            component,
            version
        )
    }

    override fun getNotExistedBuildErrorResponse(component: String, version: String): ErrorResponse {
        return get(
            HttpStatus.NOT_FOUND.value(),
            object : TypeReference<ErrorResponse>() {},
            "/rest/api/1/builds/component/{component}/version/{version}",
            emptyMap(),
            component,
            version
        )
    }

    override fun createMandatoryUpdate(
        component: String,
        version: String,
        dryRun: Boolean,
        dto: MandatoryUpdateDTO
    ): MandatoryUpdateResponseDTO {
        return post(
            200,
            object: TypeReference<MandatoryUpdateResponseDTO>() {},
            "/rest/api/1/builds/component/{component}/version/{version}/mandatory-update",
            mapOf("dryRun" to dryRun),
            dto,
            component,
            version
        )
    }

    private fun stubCreateMandatoryUpdate() {
        val cases = listOf(
            Triple(
                true,
                MandatoryUpdateFilterDTO(
                    activeLinePeriod = 180,
                    excludeComponents = emptySet(),
                    systems = emptySet()
                ),
                "../test-data/releng/create-mandatory-update-1.json"
            ),
            Triple(
                true,
                MandatoryUpdateFilterDTO(
                    activeLinePeriod = 180,
                    excludeComponents = setOf("main-component-second"),
                    systems = setOf("CLASSIC")
                ),
                "../test-data/releng/create-mandatory-update-2.json"
            ),
            Triple(
                false,
                MandatoryUpdateFilterDTO(
                    180,
                    emptySet(),
                    systems = emptySet()
                ),
                "../test-data/releng/create-mandatory-update-3.json"
            )
        )

        cases.forEach { (dryRun, filterDto, expectedJson) ->
            val expected: MandatoryUpdateResponseDTO = loadObject(
                expectedJson,
                object : TypeReference<MandatoryUpdateResponseDTO>() {}
            )
            doReturn(expected).whenever(buildService)
                .createMandatoryUpdate(
                    eq("dependency-component-first"),
                    eq("1.0.2"),
                    eq(dryRun),
                    argThat { dto -> dto.filter == filterDto }
                )
        }
    }
}