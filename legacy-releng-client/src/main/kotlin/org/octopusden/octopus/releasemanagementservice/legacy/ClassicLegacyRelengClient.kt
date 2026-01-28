package org.octopusden.octopus.releasemanagementservice.legacy

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign.Builder
import feign.Logger
import feign.Request
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.apache.http.impl.client.HttpClientBuilder
import java.util.concurrent.TimeUnit
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateRelengFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

class ClassicLegacyRelengClient(url: String, objectMapper: ObjectMapper) : LegacyRelengClient {

    constructor(url: String) : this(url, with(jacksonObjectMapper()) {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        this
    })

    private val client = Builder()
        .client(ApacheHttpClient(
            HttpClientBuilder.create()
                .disableCookieManagement()
                .build()
        ))
        .options(Request.Options(5, TimeUnit.MINUTES, 5, TimeUnit.MINUTES, true))
        .encoder(JacksonEncoder(objectMapper))
        .decoder(JacksonDecoder(objectMapper))
        .errorDecoder(LegacyRelengErrorDecoder(objectMapper))
        .logger(Slf4jLogger(LegacyRelengClient::class.java))
        .logLevel(Logger.Level.BASIC)
        .target(LegacyRelengClient::class.java, url)

    override fun getBuilds(component: String, filter: BuildFilterDTO): Collection<ShortBuildDTO> =
        client.getBuilds(component, filter)

    override fun getBuild(component: String, version: String): BuildDTO = client.getBuild(component, version)

    override fun getComponents(): Collection<ComponentDTO> = client.getComponents()

    override fun getComponent(component: String): ComponentDTO = client.getComponent(component)

    override fun updateComponent(component: String, dto: ComponentDTO): ComponentDTO =
        client.updateComponent(component, dto)

    override fun getMandatoryUpdateBuilds(component: String, version: String, filter: MandatoryUpdateRelengFilterDTO) =
        client.getMandatoryUpdateBuilds(component, version, filter)
}