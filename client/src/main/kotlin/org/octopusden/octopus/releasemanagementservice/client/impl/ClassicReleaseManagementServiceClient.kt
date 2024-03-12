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
import org.octopusden.octopus.releasemanagementservice.client.ReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.ReleaseManagementServiceErrorDecoder
import org.octopusden.octopus.releasemanagementservice.client.ReleaseManagementServiceRetry
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ServiceInfoDTO
import java.util.concurrent.TimeUnit

class ClassicReleaseManagementServiceClient(
    apiParametersProvider: ReleaseManagementServiceClientParametersProvider,
    private val mapper: ObjectMapper
) : ReleaseManagementServiceClient {
    private var client =
        createClient(apiParametersProvider.getApiUrl(), mapper, apiParametersProvider.getTimeRetryInMillis())

    constructor(apiParametersProvider: ReleaseManagementServiceClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    fun setUrl(apiUrl: String, timeRetryInMillis: Int) {
        client = createClient(apiUrl, mapper, timeRetryInMillis)
    }

    override fun getServiceInfo(): ServiceInfoDTO = client.getServiceInfo()

    companion object {
        private fun getMapper(): ObjectMapper {
            val objectMapper = jacksonObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return objectMapper
        }

        private fun createClient(
            apiUrl: String,
            objectMapper: ObjectMapper,
            timeRetryInMillis: Int
        ): ReleaseManagementServiceClient {
            return Feign.builder()
                .client(ApacheHttpClient())
                .options(Request.Options(30, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true))
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
