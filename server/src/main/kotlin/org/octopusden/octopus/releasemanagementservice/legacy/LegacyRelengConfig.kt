package org.octopusden.octopus.releasemanagementservice.legacy

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class LegacyRelengConfig(private val relengProperties: LegacyRelengProperties, private val objectMapper: ObjectMapper) {

    @Bean
    fun legacyRelengClient(): LegacyRelengClient = ClassicLegacyRelengClient(relengProperties.url, objectMapper)
        .also { log.info("LegacRelengClient initiated, url: '{}'", relengProperties.url) }

    companion object {
        private val log = LoggerFactory.getLogger(LegacyRelengConfig::class.java)
    }
}
