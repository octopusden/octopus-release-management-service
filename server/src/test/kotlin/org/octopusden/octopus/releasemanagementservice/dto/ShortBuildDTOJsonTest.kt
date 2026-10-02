package org.octopusden.octopus.releasemanagementservice.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildStatus
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import java.util.Date

class ShortBuildDTOJsonTest {
    // A plain mapper writes dates as epoch millis by default, so this pins the explicit ISO-8601 format
    @Test
    fun statusHistoryDatesAreIsoStringsTest() {
        val mapper = jacksonObjectMapper()
        val build = ShortBuildDTO("c", "1.0", BuildStatus.RELEASE, false, statusHistory = mapOf(BuildStatus.RELEASE to Date(1788968045672)))
        val json = mapper.writeValueAsString(build)
        Assertions.assertTrue(json.contains("\"RELEASE\":\"2026-09-09T15:34:05.672+00:00\""), json)
        Assertions.assertEquals(build, mapper.readValue(json, ShortBuildDTO::class.java))
    }
}
