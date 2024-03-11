package org.octopusden.octopus.releasemanagement.client.common.dto

data class ErrorResponse(val errorCode: ReleaseManagementServiceErrorCode, val errorMessage: String)
