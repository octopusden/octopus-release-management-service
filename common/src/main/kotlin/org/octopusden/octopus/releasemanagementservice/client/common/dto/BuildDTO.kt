package org.octopusden.octopus.releasemanagementservice.client.common.dto

import java.util.Date

data class BuildDTO(
    val component: String,
    val version: String,
    val status: BuildStatus,
    val dependencies: Collection<ShortBuildDTO>,
    val commits: Collection<CommitDTO>,
    val statusHistory: Map<BuildStatus, Date>
)
