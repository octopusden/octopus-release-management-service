package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class BuildParameters(
    val javaVersion: String? = null,
    val mavenVersion: String? = null
)
