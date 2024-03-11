package org.octopusden.octopus.releasemanagement.client.impl

interface ReleaseManagementServiceClientParametersProvider {
    fun getApiUrl(): String
    fun getTimeRetryInMillis(): Int
}
