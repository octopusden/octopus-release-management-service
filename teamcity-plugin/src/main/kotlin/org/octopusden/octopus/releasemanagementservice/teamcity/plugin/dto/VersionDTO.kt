package org.octopusden.octopus.releasemanagementservice.teamcity.plugin.dto

data class VersionDTO(
    val selection: BuildSelectionDTO,
    val version: String
)
