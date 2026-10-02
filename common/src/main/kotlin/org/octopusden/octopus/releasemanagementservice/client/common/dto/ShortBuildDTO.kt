package org.octopusden.octopus.releasemanagementservice.client.common.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

data class ShortBuildDTO(
    val component: String,
    val version: String,
    val status: BuildStatus,
    val hotfix: Boolean,
    val buildParameters: BuildParameters = BuildParameters(),
    /** Release line as used by the `lines` filter (a hotfix build belongs to its release version); null if unknown. */
    val lineVersion: String? = null,
    /** Same as [BuildDTO.statusHistory]; a missing entry means the date is unknown or the build is no longer in that status. */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING)
    val statusHistory: Map<BuildStatus, Date> = emptyMap(),
)
