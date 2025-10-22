package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class MandatoryUpdateFilterDTO(
    val activeLinePeriod: Int,
    val startVersion: String? = null,
    val excludeVersions: Set<String> = emptySet(),
    val excludeComponents: Set<String> = emptySet(),
    val excludeSystems: Set<String> = emptySet(),
    val isFullMatchSystems: Boolean = true
)