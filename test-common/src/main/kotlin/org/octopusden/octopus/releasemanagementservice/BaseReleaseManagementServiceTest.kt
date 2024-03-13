package org.octopusden.octopus.releasemanagementservice

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseReleaseManagementServiceTest {

    private val releaseManagementServiceVersion = System.getenv("release-management-service.version")
    abstract fun getServiceInfo(): ServiceInfoDTO
    @Test
    fun serviceInfoTest() {
        val expected = ServiceInfoDTO(ServiceInfoDTO.Build(releaseManagementServiceVersion))
        Assertions.assertEquals(expected, getServiceInfo())
    }
}
