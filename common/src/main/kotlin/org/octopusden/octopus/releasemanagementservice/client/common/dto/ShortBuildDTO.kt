package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class ShortBuildDTO(
    val component: String,
    val version: String,
    val status: BuildStatus,
    val hotfix: Boolean = false
)
