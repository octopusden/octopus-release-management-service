package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class BuildDTO(val component: String, val version: String, val status: BuildStatus, val dependencies: Collection<ShortBuildDTO>, val commits: Collection<CommitDTO>)
