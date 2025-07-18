package org.octopusden.octopus.releasemanagementservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jira1")
data class JiraClientProperties(
    val host: String,
    val username: String,
    val password: String,
)