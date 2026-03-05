package org.octopusden.octopus.releasemanagementservice.client.common.dto

import org.octopusden.octopus.releasemanagementservice.client.common.exception.AlreadyExistsException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.ArgumentsNotCompatibleException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException

enum class ReleaseManagementServiceErrorCode(private val function: (message: String) -> Exception, val simpleMessage: String) {
    OTHER(::IllegalStateException, "Internal server error"),
    NOT_FOUND(::NotFoundException, "Not Found"),
    ALREADY_EXISTS(::AlreadyExistsException, "Already exists"),
    ARGUMENTS_NOT_COMPATIBLE(::ArgumentsNotCompatibleException, "Arguments not compatible"),
    ;

    fun getException(message: String): Exception {
        return function.invoke(message)
    }

    companion object {
        fun getErrorCode(exception: Exception): ReleaseManagementServiceErrorCode {
            val qualifiedName = exception::class.qualifiedName
            return entries.find { v -> v.function.invoke("")::class.qualifiedName == qualifiedName }
                ?: OTHER
        }
    }
}
