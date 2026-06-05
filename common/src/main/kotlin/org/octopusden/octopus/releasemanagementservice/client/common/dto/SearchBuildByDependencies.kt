package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class BuildDependencySearchRequest(
    val component: String,
    val versionRange: VersionRange,
    val statuses: Set<BuildStatus> = emptySet(),
    val descending: Boolean = true,
    val limit: Int,
    val dependencies: Set<DependencySearchCriteria> = emptySet(),
    val requireAllDependencies: Boolean = false
)

data class BuildDependencySearchResult(
    val component: String,
    val version: String,
    val status: BuildStatus,
    val dependencies: Collection<ShortBuildDTO>,
    val hotfix: Boolean
)

data class DependencySearchCriteria(
    val component: String,
    val versionRange: VersionRange
)

data class VersionRange(
    val from: String,
    val to: String
)