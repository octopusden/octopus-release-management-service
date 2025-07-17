package org.octopusden.octopus.releasemanagementservice.controller

import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.service.UtilityService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/api/1/utility")
class UtilityController(private val utilityService: UtilityService) {

    @PostMapping("component/{component}/version/{version}/mandatory-update", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createMandatoryUpdate(
        @PathVariable("component") component: String,
        @PathVariable("version") version: String,
        @RequestParam(defaultValue = "true") dryRun: Boolean,
        @RequestBody dto: MandatoryUpdateDTO
    ): MandatoryUpdateResponseDTO {
        log.info("Create mandatory update: component='{}', version='{}', dryRun={}, dto={}", component, version, dryRun, dto)
        return utilityService.createMandatoryUpdate(component, version, dryRun, dto)
    }

    companion object {
        private val log = LoggerFactory.getLogger(UtilityController::class.java)
    }
}