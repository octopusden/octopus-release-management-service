package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class MandatoryUpdateResponseDTO(
    val epicKey: String?,
    val builds: Collection<BuildDTO>
)