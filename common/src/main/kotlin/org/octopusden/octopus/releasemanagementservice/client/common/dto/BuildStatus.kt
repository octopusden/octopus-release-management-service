package org.octopusden.octopus.releasemanagementservice.client.common.dto

enum class BuildStatus {
    BUILD, RC, RELEASE;

    fun noLessThan() = entries.filter { it.ordinal >= ordinal }.toSet()
}
