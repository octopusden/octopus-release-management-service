package org.octopusden.octopus.releasemanagementservice.service.impl

import org.octopusden.octopus.components.registry.client.impl.ClassicComponentsRegistryServiceClient
import org.octopusden.octopus.components.registry.client.impl.ClassicComponentsRegistryServiceClientUrlProvider
import org.octopusden.octopus.components.registry.core.dto.ComponentV1
import org.octopusden.octopus.components.registry.core.dto.DetailedComponent
import org.octopusden.octopus.releasemanagementservice.service.ComponentRegistryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ComponentRegistryServiceImpl(
    @Value("\${components-registry-service.url}")
    private val componentsRegistryServiceUrl: String
): ComponentRegistryService {
    private val client = ClassicComponentsRegistryServiceClient(
        object : ClassicComponentsRegistryServiceClientUrlProvider {
            override fun getApiUrl() = componentsRegistryServiceUrl
        }
    )

    override fun getById(id: String): ComponentV1 {
        return client.getById(id)
    }

    override fun getDetailedComponent(component: String, version: String): DetailedComponent {
        return client.getDetailedComponent(component, version)
    }
}