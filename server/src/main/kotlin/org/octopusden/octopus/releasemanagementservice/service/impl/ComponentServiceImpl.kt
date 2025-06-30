package org.octopusden.octopus.releasemanagementservice.service.impl

import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClient
import org.octopusden.octopus.releasemanagementservice.service.ComponentService
import org.springframework.stereotype.Service

@Service
class ComponentServiceImpl(private val client: LegacyRelengClient) : ComponentService {
    override fun getComponents(): Collection<ComponentDTO> = client.getComponents()

    override fun getComponent(component: String): ComponentDTO = client.getComponent(component)

    override fun updateComponent(component: String, dto: ComponentDTO): ComponentDTO = client.updateComponent(component, dto)
}