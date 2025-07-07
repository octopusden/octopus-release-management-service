package org.octopusden.octopus.releasemanagementservice.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

interface ReleaseManagementServiceClient {

    @RequestLine("GET /actuator/info")
    fun getServiceInfo(): ServiceInfoDTO

    @RequestLine("GET /rest/api/1/builds/component/{component}")
    fun getBuilds(@Param("component") component: String, @QueryMap filter: BuildFilterDTO = BuildFilterDTO()): Collection<ShortBuildDTO>

    @RequestLine("GET /rest/api/1/builds/component/{component}/version/{version}")
    fun getBuild(@Param("component") component: String, @Param("version") version: String): BuildDTO

    @RequestLine("POST /rest/api/1/builds/component/{component}/version/{version}/mandatory-update?dryRun={dryRun}")
    @Headers("Content-Type: application/json")
    fun createMandatoryUpdate(
        @Param("component") component: String,
        @Param("version") version: String,
        @Param("dryRun") dryRun: Boolean,
        dto: MandatoryUpdateDTO
    ): MandatoryUpdateResponseDTO

    @RequestLine("GET /rest/api/1/support/components")
    fun getComponents() : Collection<ComponentDTO>

    @RequestLine("GET /rest/api/1/support/components/{component}")
    fun getComponent(@Param("component") component: String) : ComponentDTO

    @RequestLine("PUT /rest/api/1/support/components/{component}")
    fun updateComponent(@Param("component") component: String, dto: ComponentDTO) : ComponentDTO
}
