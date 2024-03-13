package org.octopusden.octopus.releasemanagementservice.service

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

interface BuildService {
    fun getBuilds(component: String): Collection<ShortBuildDTO>
    fun getBuild(component: String, version: String): BuildDTO
}
