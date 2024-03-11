package org.octopusden.octopus.releasemanagement.client.common.dto

data class ServiceInfoDTO(val build: Build) {
    data class Build(val version: String)
}
