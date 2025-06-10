package org.octopusden.octopus.releasemanagementservice.legacy

import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.service.ComponentService
import org.springframework.stereotype.Service

@Service
class LegacyRelengComponentService(private val client: LegacyRelengClient) : ComponentService {
    override fun getComponents(): Collection<ComponentDTO> = client.getComponents()

    override fun getComponent(component: String): ComponentDTO = client.getComponent(component)

    override fun updateComponent(component: String, dto: ComponentDTO): ComponentDTO = client.updateComponent(component, dto)

    override fun getDependentComponents(component: String, version: String): Collection<ComponentDTO> = client.getDependentComponents(component, version)
}