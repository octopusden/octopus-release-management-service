package org.octopusden.octopus.releasemanagementservice.client.common.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

data class BuildDTO(
    val component: String,
    val version: String,
    val status: BuildStatus,
    val parents: Collection<ShortBuildDTO>,
    val dependencies: Collection<ShortBuildDTO>,
    val commits: Collection<CommitDTO>,
    /** Dates of the statuses up to the current one; a revoked release has no RELEASE entry. */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING)
    val statusHistory: Map<BuildStatus, Date>,
    val hotfix: Boolean,
    val limitations: String? = null,
    val buildParameters: BuildParameters = BuildParameters(),
    /** See [ShortBuildDTO.lineVersion]. */
    val lineVersion: String? = null,
)
