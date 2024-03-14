package org.octopusden.octopus.releasemanagementservice.legacy

import feign.Param
import feign.RequestLine
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

interface LegacyRelengClient {

    @RequestLine("GET rest/release-engineering/3/component/{component}/builds")
    fun getBuilds(@Param("component") component: String): Collection<ShortBuildDTO>

    @RequestLine("GET rest/release-engineering/3/component/{component}/version/{version}/build")
    fun getBuild(@Param("component") component: String, @Param("version") version: String): BuildDTO
}
