package org.octopusden.octopus.releasemanagementservice

import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO

class UtilityControllerTest: BaseUtilityControllerTest(), BaseReleaseManagementServiceFuncTest  {

    override fun getObjectMapper() = TestUtil.mapper

    override fun createMandatoryUpdate(
        component: String,
        version: String,
        dryRun: Boolean,
        dto: MandatoryUpdateDTO
    ): MandatoryUpdateResponseDTO =
        TestUtil.client.createMandatoryUpdate(component, version, dryRun, dto)
}