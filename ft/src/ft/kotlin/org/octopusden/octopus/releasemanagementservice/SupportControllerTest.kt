package org.octopusden.octopus.releasemanagementservice

import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ReleaseManagementServiceErrorCode
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException

class SupportControllerTest : BaseSupportControllerTest() {
    override fun getObjectMapper() = TestUtil.mapper

    override fun getComponents() = TestUtil.client.getComponents()

    override fun getComponent(component: String) = TestUtil.client.getComponent(component)

    override fun getMandatoryUpdateComponents(component: String, version: String) = TestUtil.client.getMandatoryUpdateComponents(component, version)

    override fun getNotExistedComponentErrorResponse(component: String) = try {
        TestUtil.client.getComponent(component)
        ErrorResponse(ReleaseManagementServiceErrorCode.OTHER, "Failure expected")
    } catch (e: NotFoundException) {
        ErrorResponse(ReleaseManagementServiceErrorCode.NOT_FOUND, e.message!!)
    }
}