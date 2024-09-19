package org.octopusden.octopus.releasemanagementservice.legacy

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.slf4j.LoggerFactory

class LegacyRelengErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        val responseBody = response.body().asInputStream().readAllBytes()
        return try {
            val errorResponse = objectMapper.readValue(responseBody, ErrorResponse::class.java)
            errorResponse.errorCode.getException(errorResponse.errorMessage)
        } catch (e: Exception) {
            log.error("ErrorResponse decode error", e)
            LegacyRelengClientException(response.status(), String(responseBody))
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(LegacyRelengErrorDecoder::class.java)
    }
}
