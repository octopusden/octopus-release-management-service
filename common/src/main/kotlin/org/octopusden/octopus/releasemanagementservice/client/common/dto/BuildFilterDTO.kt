package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class BuildFilterDTO(
    val statuses: Set<BuildStatus> = emptySet(),
    val minors: Set<String> = emptySet(),
    val descending: Boolean = false,
    val limit: Int? = null
)