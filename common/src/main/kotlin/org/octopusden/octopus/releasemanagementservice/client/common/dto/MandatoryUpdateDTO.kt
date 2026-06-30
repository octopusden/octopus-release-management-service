package org.octopusden.octopus.releasemanagementservice.client.common.dto

import java.util.Date

data class MandatoryUpdateDTO(
    val component: String,
    val version: String,
    val projectKey: String,
    val epicName: String,
    val issuePriority: String? = null,
    val dueDate: Date? = null,
    val notice: String = "",
    val customer: String,
    val filter: MandatoryUpdateFilterDTO
)