package org.octopusden.octopus.releasemanagementservice.legacy

import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateRelengFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

interface LegacyRelengClient {

    @RequestLine("GET rest/release-engineering/3/component/{component}/builds")
    fun getBuilds(@Param("component") component: String, @QueryMap filter: BuildFilterDTO): Collection<ShortBuildDTO>

    @RequestLine("GET rest/release-engineering/3/component/{component}/version/{version}/build")
    fun getBuild(@Param("component") component: String, @Param("version") version: String): BuildDTO

    @RequestLine("GET rest/release-engineering/3/component-management")
    fun getComponents(): Collection<ComponentDTO>

    @RequestLine("GET rest/release-engineering/3/component-management/{component}")
    fun getComponent(@Param("component") component: String): ComponentDTO

    @RequestLine("PUT rest/release-engineering/3/component-management/{component}")
    fun updateComponent(@Param("component") component: String, dto: ComponentDTO): ComponentDTO

    @RequestLine("GET rest/release-engineering/3/component/{component}/version/{version}/mandatory-update")
    fun getMandatoryUpdateBuilds(
        @Param("component") component: String,
        @Param("version") version: String,
        @QueryMap filter: MandatoryUpdateRelengFilterDTO
    ): Collection<BuildDTO>
}
