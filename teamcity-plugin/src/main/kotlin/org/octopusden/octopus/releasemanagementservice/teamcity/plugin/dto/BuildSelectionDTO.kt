package org.octopusden.octopus.releasemanagementservice.teamcity.plugin.dto

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildStatus

data class BuildSelectionDTO(
    val component: String,
    val status: BuildStatus,
    val minor: String? = null
) {
    fun toBuildFilterDTO() = BuildFilterDTO(
        status.noLessThan(),
        minor?.let { setOf(it) } ?: emptySet(),
        true,
        1
    )
}
