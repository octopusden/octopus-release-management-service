package org.octopusden.octopus.releasemanagementservice.service.impl

import org.octopusden.octopus.components.registry.client.impl.ClassicComponentsRegistryServiceClient
import org.octopusden.octopus.components.registry.client.impl.ClassicComponentsRegistryServiceClientUrlProvider
import org.octopusden.octopus.components.registry.core.dto.ComponentV1
import org.octopusden.octopus.releasemanagementservice.config.ComponentsRegistryServiceProperties
import org.octopusden.octopus.releasemanagementservice.service.ComponentRegistryService
import org.springframework.stereotype.Service

@Service
class ComponentRegistryServiceImpl(private val componentsRegistryServiceProperties: ComponentsRegistryServiceProperties): ComponentRegistryService {
    private val client = ClassicComponentsRegistryServiceClient(
        object : ClassicComponentsRegistryServiceClientUrlProvider {
            override fun getApiUrl() = componentsRegistryServiceProperties.url
        }
    )

    override fun getById(id: String): ComponentV1 {
        return client.getById(id)
    }
}