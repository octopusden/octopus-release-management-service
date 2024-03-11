package org.octopusden.octopus.releasemanagement.client

import feign.RequestLine
import org.octopusden.octopus.releasemanagement.client.common.dto.ServiceInfoDTO

interface ReleaseManagementServiceClient {
    @RequestLine("GET /actuator/info")
    fun getServiceInfo(): ServiceInfoDTO
}
