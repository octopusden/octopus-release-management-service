package org.octopusden.octopus.releasemanagementservice.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.http.HttpStatus

class ReleaseManagementServiceErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder.Default() {

    override fun decode(methodKey: String?, response: Response?): Exception {
        return getErrorResponse(response)
            ?.let {
                val status = response?.status()!!
                ERROR_RESPONSE_CODES.getOrDefault(status) { super.decode(methodKey, response) }
                    .invoke(it)
            } ?: super.decode(methodKey, response)
    }

    private fun getErrorResponse(response: Response?): ErrorResponse? {
        return response?.let { res ->
            res.headers()["content-type"]
                ?.find { it.contains("application/json") }
                ?.let {
                    try {
                        res.body()
                            ?.asInputStream()
                            .use { inputStream -> objectMapper.readValue(inputStream, ErrorResponse::class.java) }
                    } catch (e: Exception) {
                        null
                    }
                }
        }
    }

    companion object {
        private val errorResponseFunction =
            { errorResponse: ErrorResponse -> errorResponse.errorCode.getException(errorResponse.errorMessage) }
        private val ERROR_RESPONSE_CODES: Map<Int, (ErrorResponse) -> Exception> = mapOf(
            HttpStatus.SC_NOT_FOUND to errorResponseFunction,
            HttpStatus.SC_INTERNAL_SERVER_ERROR to errorResponseFunction
        )
    }
}
