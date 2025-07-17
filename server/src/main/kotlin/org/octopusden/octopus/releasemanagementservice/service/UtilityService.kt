package org.octopusden.octopus.releasemanagementservice.service

import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO

interface UtilityService {
    fun createMandatoryUpdate(component: String, version: String, dryRun: Boolean, dto: MandatoryUpdateDTO): MandatoryUpdateResponseDTO
}