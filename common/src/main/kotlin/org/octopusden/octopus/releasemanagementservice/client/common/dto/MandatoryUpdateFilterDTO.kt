package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class MandatoryUpdateFilterDTO(
    val activeLinePeriod: Int,
    val excludeComponents: Set<String> = emptySet(),
    val excludeSystems: Set<String> = emptySet()
)