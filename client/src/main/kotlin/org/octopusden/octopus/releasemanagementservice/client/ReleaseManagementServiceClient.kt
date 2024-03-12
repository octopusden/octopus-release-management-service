package org.octopusden.octopus.releasemanagementservice.client

import feign.RequestLine
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO

interface ReleaseManagementServiceClient {
    @RequestLine("GET /actuator/info")
    fun getServiceInfo(): ServiceInfoDTO
}
