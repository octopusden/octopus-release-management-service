package org.octopusden.octopus.releasemanagementservice.controller

import org.octopusden.octopus.releasemanagementservice.client.common.dto.IssueReleasesDTO
import org.octopusden.octopus.releasemanagementservice.service.IssueService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rest/api/1/issues")
class IssueController(private val issueService: IssueService) {

    @GetMapping("{issueKey}/releases", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getIssueReleases(@PathVariable issueKey: String): IssueReleasesDTO {
        log.info("Get issue releases for '{}'", issueKey)
        return issueService.getIssueReleases(issueKey)
    }

    companion object {
        private val log = LoggerFactory.getLogger(IssueController::class.java)
    }

}
