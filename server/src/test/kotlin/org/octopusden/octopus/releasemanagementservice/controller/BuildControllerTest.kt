package org.octopusden.octopus.releasemanagementservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.octopusden.octopus.releasemanagementservice.BaseBuildControllerTest
import org.octopusden.octopus.releasemanagementservice.ReleaseManagementServiceApplication
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
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
class BuildControllerTest : BaseBuildControllerTest(), BaseControllerTest {

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var mapper: ObjectMapper
    override fun getMockMvc(): MockMvc = mvc
    override fun getObjectMapper(): ObjectMapper = mapper

    override fun getBuilds(component: String): Collection<ShortBuildDTO> {
        return get(
            HttpStatus.OK.value(),
            object : TypeReference<Collection<ShortBuildDTO>>() {},
            "/rest/api/1/builds/component/{component}",
            component
        )
    }

    override fun getBuild(component: String, version: String): BuildDTO {
        return get(
            HttpStatus.OK.value(),
            object : TypeReference<BuildDTO>() {},
            "/rest/api/1/builds/component/{component}/version/{version}",
            component,
            version
        )
    }

    override fun getNotExistedBuildErrorResponse(component: String, version: String): ErrorResponse {
        return get(
            HttpStatus.NOT_FOUND.value(),
            object : TypeReference<ErrorResponse>() {},
            "/rest/api/1/builds/component/{component}/version/{version}",
            component,
            version
        )
    }
}