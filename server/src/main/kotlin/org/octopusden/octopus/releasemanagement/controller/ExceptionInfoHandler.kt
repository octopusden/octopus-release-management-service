package org.octopusden.octopus.releasemanagement.controller

import org.octopusden.octopus.releasemanagement.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagement.client.common.dto.ReleaseManagementServiceErrorCode
import org.octopusden.octopus.releasemanagement.client.common.exception.ArgumentsNotCompatibleException
import org.octopusden.octopus.releasemanagement.client.common.exception.NotFoundException
import org.octopusden.octopus.releasemanagement.client.common.exception.ReleaseManagementServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ExceptionInfoHandler {

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleNotFound(exception: ReleaseManagementServiceException) = getErrorResponse(exception)

    @ExceptionHandler(ArgumentsNotCompatibleException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleArgumentsNotCompatible(exception: ReleaseManagementServiceException): ErrorResponse {
        log.error(exception.message)
        return getErrorResponse(exception)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @Order(100)
    fun handleException(exception: Exception): ErrorResponse {
        log.error(exception.message ?: "Internal error", exception)
        return getErrorResponse(exception)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ExceptionInfoHandler::class.java)

        private fun getErrorResponse(exception: Exception): ErrorResponse {
            val errorCode = ReleaseManagementServiceErrorCode.getErrorCode(exception)
            return ErrorResponse(
                errorCode, exception.message
                    ?: errorCode.simpleMessage
            )
        }
    }
}
