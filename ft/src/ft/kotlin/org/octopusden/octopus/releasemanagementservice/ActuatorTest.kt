package org.octopusden.octopus.releasemanagementservice

import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO

class ActuatorTest : BaseReleaseManagementServiceFuncTest() {

    override fun getServiceInfo(): ServiceInfoDTO = client.getServiceInfo()
}
