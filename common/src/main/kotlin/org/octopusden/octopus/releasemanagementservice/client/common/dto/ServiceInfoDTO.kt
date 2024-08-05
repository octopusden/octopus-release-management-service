package org.octopusden.octopus.releasemanagementservice.client.common.dto

import com.fasterxml.jackson.annotation.JsonValue

data class ServiceInfoDTO(val build: Build) {
    @Suppress("unused")
    enum class ServiceName(
        @get:JsonValue
        val jsonValue: String
    ) { RELEASE_MANAGEMENT_SERVICE("release-management-service") }

    data class Build(
        val name: ServiceName,
        val version: String
    )
}
