package org.octopusden.octopus.releasemanagementservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.octopusden.octopus.releasemanagementservice.BaseIssueControllerTest
import org.octopusden.octopus.releasemanagementservice.ReleaseManagementServiceApplication
import org.octopusden.octopus.releasemanagementservice.client.common.dto.IssueReleasesDTO
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
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("test")
class IssueControllerTest :
    BaseIssueControllerTest(),
    BaseControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    override fun getMockMvc(): MockMvc = mvc

    override fun getObjectMapper(): ObjectMapper = mapper

    override fun getIssueReleases(issueKey: String): IssueReleasesDTO =
        get(
            HttpStatus.OK.value(),
            object : TypeReference<IssueReleasesDTO>() {},
            "/rest/api/1/issues/{issueKey}/releases",
            emptyMap(),
            issueKey,
        )
}
