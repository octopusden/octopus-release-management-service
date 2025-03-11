package org.octopusden.octopus.releasemanagementservice.teamcity.plugin.dto

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildStatus

data class BuildSelectionDTO(
    val component: String,
    val status: BuildStatus? = null,
    val minor: String? = null,
    val inReleaseBranch: Boolean? = null
) {
    fun toBuildFilterDTO() = BuildFilterDTO(
        statuses = status?.noLessThan() ?: emptySet(),
        minors = minor?.let { setOf(it) } ?: emptySet(),
        inReleaseBranch = inReleaseBranch,
        descending = true,
        limit = 1
    )
}
