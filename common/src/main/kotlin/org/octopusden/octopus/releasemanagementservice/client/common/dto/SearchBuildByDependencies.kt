package org.octopusden.octopus.releasemanagementservice.client.common.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class BuildDependencySearchRequest(
    @field:NotBlank
    val component: String,
    @field:Valid
    val versionRange: VersionRange,
    val statuses: Set<BuildStatus> = emptySet(),
    val descending: Boolean = false,
    @field:Positive
    val limit: Int? = null,
    @field:Valid
    val dependencies: Set<DependencySearchCriteria> = emptySet(),
    val requireAllDependencies: Boolean = false,
)

data class BuildDependencySearchResult(
    val component: String,
    val version: String,
    val status: BuildStatus,
    val dependencies: Collection<ShortBuildDTO>,
    val hotfix: Boolean
)

data class DependencySearchCriteria(
    @field:NotBlank
    val component: String,
    @field:Valid
    val versionRange: VersionRange
)

data class VersionRange(
    @field:NotBlank
    val from: String,
    @field:NotBlank
    val to: String
)