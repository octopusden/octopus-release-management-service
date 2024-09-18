package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class CommitDTO(val repository: String, val sha: String, val branch: String?, val inReleaseBranch: Boolean?)
