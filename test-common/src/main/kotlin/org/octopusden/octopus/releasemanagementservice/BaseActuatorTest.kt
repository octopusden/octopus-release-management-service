package org.octopusden.octopus.releasemanagementservice

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO

abstract class BaseActuatorTest : BaseReleaseManagementServiceTest {

    private val releaseManagementServiceVersion: String = System.getenv("release-management-service.version")
        ?: throw IllegalStateException("Environment variable 'release-management-service.version' must be provided")

    abstract fun getServiceInfo(): ServiceInfoDTO

    @Test
    fun serviceInfoTest() {
        val expected = ServiceInfoDTO(ServiceInfoDTO.Build(releaseManagementServiceVersion))
        Assertions.assertEquals(expected, getServiceInfo())
    }
}