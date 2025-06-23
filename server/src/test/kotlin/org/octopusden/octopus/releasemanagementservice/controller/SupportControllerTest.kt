package org.octopusden.octopus.releasemanagementservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.octopusden.octopus.releasemanagementservice.BaseSupportControllerTest
import org.octopusden.octopus.releasemanagementservice.ReleaseManagementServiceApplication
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
class SupportControllerTest : BaseSupportControllerTest(), BaseControllerTest {

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var mapper: ObjectMapper
    override fun getMockMvc(): MockMvc = mvc
    override fun getObjectMapper(): ObjectMapper = mapper

    override fun getComponents(): Collection<ComponentDTO> {
        return get(
            HttpStatus.OK.value(),
            object : TypeReference<Collection<ComponentDTO>>() {},
            "/rest/api/1/support/components",
            emptyMap()
        )
    }

    override fun getComponent(component: String): ComponentDTO {
        return get(
            HttpStatus.OK.value(),
            object : TypeReference<ComponentDTO>() {},
            "/rest/api/1/support/components/{component}",
            emptyMap(),
            component
        )
    }

    override fun getNotExistedComponentErrorResponse(component: String): ErrorResponse {
        return get(
            HttpStatus.NOT_FOUND.value(),
            object : TypeReference<ErrorResponse>() {},
            "/rest/api/1/support/components/{component}",
            emptyMap(),
            component
        )
    }
}