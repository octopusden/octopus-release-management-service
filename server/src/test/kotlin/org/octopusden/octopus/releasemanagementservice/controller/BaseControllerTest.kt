package org.octopusden.octopus.releasemanagementservice.controller

import com.fasterxml.jackson.core.type.TypeReference
import org.octopusden.octopus.releasemanagementservice.BaseReleaseManagementServiceTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

interface BaseControllerTest : BaseReleaseManagementServiceTest {

    fun getMockMvc(): MockMvc
    fun <T : Any> get(code: Int, typeReference: TypeReference<T>, path: String, vararg uriVars: String) =
        getMockMvc().perform(
            MockMvcRequestBuilders.get(path, *uriVars)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().`is`(code))
            .andReturn()
            .response.toObject(typeReference)

   fun <T> MockHttpServletResponse.toObject(typeReference: TypeReference<T>): T =
        getObjectMapper().readValue(this.contentAsByteArray, typeReference)

    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern(ISO_PATTERN)
            .withZone(ZoneId.systemDefault())
    }
}
