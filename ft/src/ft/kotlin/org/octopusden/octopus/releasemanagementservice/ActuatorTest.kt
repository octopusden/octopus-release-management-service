package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider

class ActuatorTest : BaseActuatorTest(), BaseReleaseManagementServiceFuncTest {

    override fun getObjectMapper(): ObjectMapper = mapper

    override fun getServiceInfo(): ServiceInfoDTO = client.getServiceInfo()

    companion object {
        @JvmStatic
        private val mapper = jacksonObjectMapper()
        @JvmStatic
        private val client =
            ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = "http://localhost:8080"
                override fun getTimeRetryInMillis() = 180000
            })
    }
}
