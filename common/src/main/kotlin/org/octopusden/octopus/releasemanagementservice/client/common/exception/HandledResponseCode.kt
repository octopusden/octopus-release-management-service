package org.octopusden.octopus.releasemanagementservice.client.common.exception

import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse

enum class HandledResponseCode(val responseCode: Int, val exceptionFunction: (ErrorResponse) -> Nothing) {
    BAD_REQUESTS(400, { response -> throw NotFoundException(response.errorMessage) }),
    INTERNAL_SERVER_ERROR(500, { response -> throw IllegalStateException(response.errorMessage) });

    companion object {
        fun mappedException(responseCode: Int): HandledResponseCode? {
            return values().find { it.responseCode == responseCode }
        }
    }
}
