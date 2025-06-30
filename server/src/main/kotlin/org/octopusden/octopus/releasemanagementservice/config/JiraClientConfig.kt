package org.octopusden.octopus.releasemanagementservice.config

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class JiraClientConfig(private val props: JiraClientProperties) {
    @Bean
    fun jiraRestClient(): JiraRestClient {
        return AsynchronousJiraRestClientFactory()
            .createWithBasicHttpAuthentication(URI(props.host), props.username, props.password)
    }
}