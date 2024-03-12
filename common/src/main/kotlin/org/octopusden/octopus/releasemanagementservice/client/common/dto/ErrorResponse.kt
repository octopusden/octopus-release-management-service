package org.octopusden.octopus.releasemanagementservice.client.common.dto

data class ErrorResponse(val errorCode: ReleaseManagementServiceErrorCode, val errorMessage: String)
