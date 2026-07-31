package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class BuildFilterDTO(
    val statuses: Set<BuildStatus> = emptySet(),
    val minors: Set<String> = emptySet(),
    val lines: Set<String> = emptySet(),
    val versions: Set<String> = emptySet(),
    val branchNames: Set<String> = emptySet(),
    val inReleaseBranch: Boolean? = null,
    val descending: Boolean = false,
    val limit: Int? = null,
    val maxAgeBuilds: Int? = null,
    val javaVersions: Set<String> = emptySet(),
    val javaVersionPresent: Boolean? = null,
    val mavenVersions: Set<String> = emptySet(),
    val mavenVersionPresent: Boolean? = null,
)
