package org.octopusden.octopus.releasemanagementservice

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ReleaseManagementServiceErrorCode
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException

class BuildControllerTest : BaseBuildControllerTest(), BaseReleaseManagementServiceFuncTest {

    override fun getObjectMapper() = TestUtil.mapper

    override fun getBuilds(component: String, params: Map<String, Any>) =
        TestUtil.client.getBuilds(component, TestUtil.mapper.convertValue(params, BuildFilterDTO::class.java))

    override fun getBuild(component: String, version: String) = TestUtil.client.getBuild(component, version)

    override fun getNotExistedBuildErrorResponse(component: String, version: String) = try {
        TestUtil.client.getBuild(component, version)
        ErrorResponse(ReleaseManagementServiceErrorCode.OTHER, "Failure expected")
    } catch (e: NotFoundException) {
        ErrorResponse(ReleaseManagementServiceErrorCode.NOT_FOUND, e.message!!)
    }
}
