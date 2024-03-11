package org.octopusden.octopus.releasemanagement

import org.octopusden.octopus.releasemanagement.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagement.client.impl.ReleaseManagementServiceClientParametersProvider

abstract class BaseReleaseManagementServiceFuncTest : BaseReleaseManagementServiceTest() {
    companion object {
        @JvmStatic
        protected val client =
            ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = "http://localhost:8080"
                override fun getTimeRetryInMillis() = 180000
            })
    }
}
