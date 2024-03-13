package org.octopusden.octopus.releasemanagementservice.legacy

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("releng")
data class LegacyRelengProperties(val host: String)
