package org.octopusden.octopus.releasemanagementservice.controller

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import org.octopusden.octopus.releasemanagementservice.service.BuildService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("rest/api/1/builds")
class BuildController(private val buildService: BuildService) {

    @GetMapping("component/{component}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getComponentBuilds(@PathVariable("component") component: String, filter: BuildFilterDTO): Collection<ShortBuildDTO> {
        log.info("Get builds of '{}', filter: '{}'", component, filter)
        return buildService.getBuilds(component, filter)
    }

    @GetMapping("component/{component}/version/{version}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getBuild(@PathVariable("component") component: String, @PathVariable("version") version: String): BuildDTO {
        log.info("Get build '{}:{}'", component, version)
        return buildService.getBuild(component, version)
    }

    @PostMapping("component/{component}/version/{version}/mandatory-update", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createMandatoryUpdate(
        @PathVariable("component") component: String,
        @PathVariable("version") version: String,
        @RequestParam(defaultValue = "true") dryRun: Boolean,
        @RequestBody dto: MandatoryUpdateDTO
    ): MandatoryUpdateResponseDTO {
        log.info("Create mandatory update: component='{}', version='{}', dryRun={}, dto={}", component, version, dryRun, dto)
        return buildService.createMandatoryUpdate(component, version, dryRun, dto)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BuildController::class.java)
    }
}
