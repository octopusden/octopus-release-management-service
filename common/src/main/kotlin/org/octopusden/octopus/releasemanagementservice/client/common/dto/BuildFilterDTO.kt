package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class BuildFilterDTO(
    val statuses: Set<BuildStatus> = emptySet(),
    val minors: Set<String> = emptySet(),
    val lines: Set<String> = emptySet(),
    val versions: Set<String> = emptySet(),
    val inReleaseBranch: Boolean? = null,
    val descending: Boolean = false,
    val limit: Int? = null
)