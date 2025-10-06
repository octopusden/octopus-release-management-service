package org.octopusden.octopus.releasemanagementservice.client.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.Request
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import java.util.concurrent.TimeUnit
import org.octopusden.octopus.releasemanagementservice.client.ReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.ReleaseManagementServiceErrorDecoder
import org.octopusden.octopus.releasemanagementservice.client.ReleaseManagementServiceRetry
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO

class ClassicReleaseManagementServiceClient(
    apiParametersProvider: ReleaseManagementServiceClientParametersProvider,
    private val mapper: ObjectMapper
) : ReleaseManagementServiceClient {
    private var client =
        createClient(
            apiParametersProvider.getApiUrl(),
            mapper,
            apiParametersProvider.getTimeRetryInMillis(),
            apiParametersProvider.getConnectTimeoutInMillis(),
            apiParametersProvider.getReadTimeoutInMillis()
        )

    constructor(apiParametersProvider: ReleaseManagementServiceClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    override fun getBuilds(component: String, filter: BuildFilterDTO): Collection<ShortBuildDTO> =
        client.getBuilds(component, filter)

    override fun getBuild(component: String, version: String): BuildDTO = client.getBuild(component, version)

    override fun getComponents(): Collection<ComponentDTO> = client.getComponents()

    override fun getComponent(component: String): ComponentDTO = client.getComponent(component)

    override fun updateComponent(component: String, dto: ComponentDTO): ComponentDTO =
        client.updateComponent(component, dto)

    override fun createMandatoryUpdate(dryRun: Boolean, dto: MandatoryUpdateDTO): MandatoryUpdateResponseDTO =
        client.createMandatoryUpdate(dryRun, dto)

    fun setUrl(apiUrl: String, timeRetryInMillis: Int, connectTimeoutInMillis: Int, readTimeoutInMillis: Int) {
        client = createClient(
            apiUrl,
            mapper,
            timeRetryInMillis,
            connectTimeoutInMillis,
            readTimeoutInMillis
        )
    }

    override fun getServiceInfo(): ServiceInfoDTO = client.getServiceInfo()

    companion object {
        private fun getMapper(): ObjectMapper = with(jacksonObjectMapper()) {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        private fun createClient(
            apiUrl: String,
            objectMapper: ObjectMapper,
            timeRetryInMillis: Int,
            connectTimeoutInMillis: Int,
            readTimeoutInMillis: Int
        ): ReleaseManagementServiceClient {
            return Feign.builder()
                .client(ApacheHttpClient())
                .options(Request.Options(connectTimeoutInMillis.toLong(), TimeUnit.MILLISECONDS, readTimeoutInMillis.toLong(), TimeUnit.MILLISECONDS, true))
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .errorDecoder(ReleaseManagementServiceErrorDecoder(objectMapper))
                .retryer(ReleaseManagementServiceRetry(timeRetryInMillis))
                .logger(Slf4jLogger(ReleaseManagementServiceClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(ReleaseManagementServiceClient::class.java, apiUrl)
        }
    }
}
