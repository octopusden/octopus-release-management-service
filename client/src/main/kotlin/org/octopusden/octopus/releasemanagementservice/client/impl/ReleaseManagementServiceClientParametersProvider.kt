package org.octopusden.octopus.releasemanagementservice.client.impl

interface ReleaseManagementServiceClientParametersProvider {
    fun getApiUrl(): String
    fun getTimeRetryInMillis(): Int
    fun getConnectTimoutInMillis(): Int
    fun getReadTimoutInMillis(): Int
}
