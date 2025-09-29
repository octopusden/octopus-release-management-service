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
        private val hostReleaseManagement = System.getProperty("test.release-management-host")
            ?: throw Exception("System property 'test.release-management-host' must be defined")
        @JvmStatic
        private val mapper = jacksonObjectMapper()
        @JvmStatic
        private val client =
            ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = "http://$hostReleaseManagement"
                override fun getTimeRetryInMillis() = 180000
            })
    }
}
