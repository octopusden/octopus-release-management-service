package org.octopusden.octopus.releasemanagementservice.controller

import org.octopusden.octopus.releasemanagementservice.client.common.dto.ComponentDTO
import org.octopusden.octopus.releasemanagementservice.service.ComponentService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/api/1/support")
class SupportController(private val componentService: ComponentService) {

    @GetMapping("components", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getComponents(): Collection<ComponentDTO> = componentService.getComponents()

    @GetMapping("components/{component}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getComponent(@PathVariable("component") component: String): ComponentDTO =
        componentService.getComponent(component)

    @PutMapping("components/{component}", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateComponent(@PathVariable("component") component: String, @RequestBody dto: ComponentDTO): ComponentDTO =
        componentService.updateComponent(component, dto)

    @GetMapping("components/{component}/version/{version}/dependent-on", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMandatoryUpdateComponents(@PathVariable("component") component: String, @PathVariable("version") version: String): Collection<ComponentDTO> =
        componentService.getMandatoryUpdateComponents(component, version)
}