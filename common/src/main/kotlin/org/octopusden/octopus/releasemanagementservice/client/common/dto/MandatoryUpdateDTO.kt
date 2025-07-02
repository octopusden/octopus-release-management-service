package org.octopusden.octopus.releasemanagementservice.client.common.dto

import java.util.Date

data class MandatoryUpdateDTO(
    val projectKey: String,
    val epicName: String,
    val dueDate: Date,
    val notice: String = "",
    val customer: String,
    val filter: MandatoryUpdateFilterDTO
)