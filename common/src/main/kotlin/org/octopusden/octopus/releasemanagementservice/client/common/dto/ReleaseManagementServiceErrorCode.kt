package org.octopusden.octopus.releasemanagementservice.client.common.dto

import org.octopusden.octopus.releasemanagementservice.client.common.exception.AlreadyExistsException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.ArgumentsNotCompatibleException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException

enum class ReleaseManagementServiceErrorCode(private val function: (message: String) -> Exception, val simpleMessage: String) {
    OTHER({ m -> IllegalStateException(m) }, "Internal server error"),
    NOT_FOUND({ m -> NotFoundException(m) }, "Not Found"),
    ALREADY_EXISTS({m -> AlreadyExistsException(m) }, "Already exists"),
    ARGUMENTS_NOT_COMPATIBLE({ m -> ArgumentsNotCompatibleException(m) }, "Arguments not compatible");

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
