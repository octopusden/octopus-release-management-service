package org.octopusden.octopus.releasemanagementservice.service

import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO

interface ComponentService {
    fun getComponents(): Collection<ComponentDTO>
    fun getComponent(component: String): ComponentDTO
    fun updateComponent(component: String, dto: ComponentDTO): ComponentDTO
    fun getMandatoryUpdateComponents(component: String, version: String): Collection<ComponentDTO>
}