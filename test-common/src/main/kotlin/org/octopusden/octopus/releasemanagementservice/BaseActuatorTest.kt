package org.octopusden.octopus.releasemanagementservice

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO

abstract class BaseActuatorTest : BaseReleaseManagementServiceTest {
    private val releaseManagementServiceVersion = System.getProperty("release-management-service.version")
        ?: throw IllegalStateException("System property 'release-management-service.version' must be provided")

    abstract fun getServiceInfo(): ServiceInfoDTO

    @Test
    fun serviceInfoTest() {
        val expected = ServiceInfoDTO(ServiceInfoDTO.Build(ServiceInfoDTO.ServiceName.RELEASE_MANAGEMENT_SERVICE, releaseManagementServiceVersion))
        Assertions.assertEquals(expected, getServiceInfo())
    }
}