package org.octopusden.octopus.releasemanagementservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.octopusden.octopus.releasemanagementservice.BaseReleaseManagementServiceTest
import org.octopusden.octopus.releasemanagementservice.ReleaseManagementServiceApplication
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private const val ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
typealias CheckError = (Pair<Int, String>) -> Unit

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [ReleaseManagementServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
abstract class BaseControllerTest : BaseReleaseManagementServiceTest() {

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var mapper: ObjectMapper

    @BeforeAll
    fun beforeAllRepositoryControllerTests() {
        mapper.setLocale(Locale.ENGLISH)
    }

    protected fun <T> checkResponse(
        response: MockHttpServletResponse,
        status: Int,
        typeReference: TypeReference<T>,
        checkSuccess: (T) -> Unit,
        checkError: CheckError
    ) {
        if (HttpStatus.OK == HttpStatus.valueOf(status)) {
            checkSuccess(response.toObject(typeReference))
        } else {
            val err = response.toObject(object : TypeReference<ErrorResponse>() {})
            checkError(Pair(response.status, err.errorMessage))
        }
    }

    protected fun <T> MockHttpServletResponse.toObject(typeReference: TypeReference<T>): T =
        mapper.readValue(this.contentAsByteArray, typeReference)

    protected fun Date.toReleaseManagementServiceFormat(): String {
        return FORMATTER.format(toInstant())
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern(ISO_PATTERN)
            .withZone(ZoneId.systemDefault())
    }
}
