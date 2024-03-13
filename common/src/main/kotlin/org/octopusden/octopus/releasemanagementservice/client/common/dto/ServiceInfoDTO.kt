package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class ServiceInfoDTO(val build: Build) {
    data class Build(val version: String)
}
