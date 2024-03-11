package org.octopusden.octopus.releasemanagement.controller

import com.fasterxml.jackson.core.type.TypeReference
import org.octopusden.octopus.releasemanagement.client.common.dto.ServiceInfoDTO
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class ActuatorTest : BaseControllerTest() {

    override fun getServiceInfo(): ServiceInfoDTO =
        mvc.perform(
            MockMvcRequestBuilders.get("/actuator/info")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().`is`(200))
            .andReturn()
            .response.toObject(object : TypeReference<ServiceInfoDTO>() {})
}
