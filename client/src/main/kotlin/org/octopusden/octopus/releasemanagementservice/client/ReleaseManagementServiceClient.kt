package org.octopusden.octopus.releasemanagementservice.client

import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

interface ReleaseManagementServiceClient {

    @RequestLine("GET /actuator/info")
    fun getServiceInfo(): ServiceInfoDTO

    @RequestLine("GET /rest/api/1/builds/component/{component}")
    fun getBuilds(@Param("component") component: String, @QueryMap filter: BuildFilterDTO = BuildFilterDTO()): Collection<ShortBuildDTO>

    @RequestLine("GET /rest/api/1/builds/component/{component}/version/{version}")
    fun getBuild(@Param("component") component: String, @Param("version") version: String): BuildDTO
}
