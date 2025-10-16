package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class MandatoryUpdateRelengFilterDTO(
    val activeLinePeriod: Int,
    val startVersion: String? = null,
    val excludeVersion: Set<String> = emptySet()
)