package org.octopusden.octopus.releasemanagementservice.service

import org.octopusden.octopus.components.registry.core.dto.ComponentV1

interface ComponentRegistryService {
    fun getById(id: String): ComponentV1
}