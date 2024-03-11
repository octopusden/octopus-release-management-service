package org.octopusden.octopus.releasemanagement

import org.octopusden.octopus.releasemanagement.client.common.dto.ServiceInfoDTO

class ActuatorTest : BaseReleaseManagementServiceFuncTest() {

    override fun getServiceInfo(): ServiceInfoDTO = client.getServiceInfo()
}
