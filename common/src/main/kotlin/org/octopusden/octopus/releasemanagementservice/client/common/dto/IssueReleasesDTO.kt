package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class IssueReleasesDTO(
    val releases: List<ComponentVersionDTO>,
)
