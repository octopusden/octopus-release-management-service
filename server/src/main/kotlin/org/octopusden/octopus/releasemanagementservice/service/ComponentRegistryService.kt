package org.octopusden.octopus.releasemanagementservice.service

import org.octopusden.octopus.components.registry.core.dto.ComponentV1
import org.octopusden.octopus.components.registry.core.dto.DetailedComponent

interface ComponentRegistryService {
    fun getById(id: String): ComponentV1
    fun getDetailedComponent(component: String, version: String): DetailedComponent
}