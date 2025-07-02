package org.octopusden.octopus.releasemanagementservice.service

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

interface BuildService {
    fun getBuilds(component: String, filter: BuildFilterDTO): Collection<ShortBuildDTO>
    fun getBuild(component: String, version: String): BuildDTO
    fun createMandatoryUpdate(component: String, version: String, dryRun: Boolean, dto: MandatoryUpdateDTO): MandatoryUpdateResponseDTO
}