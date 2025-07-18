package org.octopusden.octopus.releasemanagementservice.service.impl

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClient
import org.octopusden.octopus.releasemanagementservice.service.BuildService
import org.springframework.stereotype.Service

@Service
class BuildServiceImpl(private val client: LegacyRelengClient) : BuildService {
    override fun getBuilds(component: String, filter: BuildFilterDTO): Collection<ShortBuildDTO> = client.getBuilds(component, filter)

    override fun getBuild(component: String, version: String): BuildDTO = client.getBuild(component, version)
}