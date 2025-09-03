package org.octopusden.octopus.releasemanagementservice.controller

import com.atlassian.jira.rest.client.api.RestClientException
import jakarta.servlet.http.HttpServletResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ReleaseManagementServiceErrorCode
import org.octopusden.octopus.releasemanagementservice.client.common.exception.ArgumentsNotCompatibleException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.ReleaseManagementServiceException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.ResourceAlreadyExistException
import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClientException
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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun handleNotFound(exception: ReleaseManagementServiceException): ErrorResponse = getErrorResponse(exception)

    @ExceptionHandler(ResourceAlreadyExistException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    fun handleResourceAlreadyExist(exception: ReleaseManagementServiceException): ErrorResponse = getErrorResponse(exception)

    @ExceptionHandler(ArgumentsNotCompatibleException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun handleArgumentsNotCompatible(exception: ReleaseManagementServiceException): ErrorResponse = getErrorResponse(exception)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @Order(100)
    fun handleException(exception: Exception): ErrorResponse = getErrorResponse(exception)

    @ExceptionHandler(LegacyRelengClientException::class)
    @ResponseBody
    fun handleLegacyClientException(exception: LegacyRelengClientException, response: HttpServletResponse): ErrorResponse {
        val status = HttpStatus.resolve(exception.code) ?: HttpStatus.INTERNAL_SERVER_ERROR
        response.status = status.value()
        return getErrorResponse(exception)
    }

    @ExceptionHandler(RestClientException::class)
    @ResponseBody
    fun handleRestClientException(exception: RestClientException, response: HttpServletResponse): ErrorResponse {
        val status = exception.statusCode.orNull()?.let { HttpStatus.resolve(it) } ?: HttpStatus.INTERNAL_SERVER_ERROR
        response.status = status.value()
        return getErrorResponse(exception)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ExceptionInfoHandler::class.java)

        private fun getErrorResponse(exception: Exception): ErrorResponse {
            log.error(exception.message ?: "Unexpected error", exception)
            val errorCode = ReleaseManagementServiceErrorCode.getErrorCode(exception)
            return ErrorResponse(
                errorCode, exception.message
                    ?: errorCode.simpleMessage
            )
        }
    }
}
