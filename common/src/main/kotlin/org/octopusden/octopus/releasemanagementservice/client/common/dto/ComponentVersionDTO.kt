package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class ComponentVersionDTO(
    val component: String,
    val version: String,
    val parents: Collection<ComponentVersionDTO>,
)
