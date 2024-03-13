package org.octopusden.octopus.releasemanagementservice.legacy

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign.Builder
import feign.Logger
import feign.Request
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit


@Configuration
class LegacyRelengConfig(private val relengProperties: LegacyRelengProperties, private val objectMapper: ObjectMapper) {

    @Bean
    fun legacyRelengClient(): LegacyRelengClient = Builder()
        .client(ApacheHttpClient())
        .options(Request.Options(5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES, true))
        .encoder(JacksonEncoder(objectMapper))
        .decoder(JacksonDecoder(objectMapper))
        .errorDecoder(LegacyRelengErrorDecoder(objectMapper))
        .logger(Slf4jLogger(LegacyRelengClient::class.java))
        .logLevel(Logger.Level.BASIC)
        .target(LegacyRelengClient::class.java, relengProperties.host)
}
