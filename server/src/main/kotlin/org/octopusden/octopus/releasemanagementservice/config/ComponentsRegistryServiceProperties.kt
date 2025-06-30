package org.octopusden.octopus.releasemanagementservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "components-registry-service")
data class ComponentsRegistryServiceProperties(val url: String)