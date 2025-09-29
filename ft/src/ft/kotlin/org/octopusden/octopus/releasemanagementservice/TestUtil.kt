package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider

class TestUtil private constructor() {
    companion object {
        private val hostReleaseManagement = System.getProperty("test.release-management-host")
            ?: throw Exception("System property 'test.release-management-host' must be defined")

        @JvmStatic
        val mapper = jacksonObjectMapper()

        @JvmStatic
        val client =
            ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = "http://$hostReleaseManagement"
                override fun getTimeRetryInMillis() = 180000
            })

        @JvmStatic
        fun executeAutomation(command: String, vararg options: String) =
            org.octopusden.octopus.automation.releasemanagement.main(
                arrayOf("--url=http://$hostReleaseManagement", command, *options)
            )
    }
}