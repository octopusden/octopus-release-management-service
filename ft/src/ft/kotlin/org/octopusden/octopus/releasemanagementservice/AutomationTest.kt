package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.automation.releasemanagement.command.DependenciesStatus
import org.octopusden.octopus.automation.releasemanagement.command.MandatoryUpdate
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateResponseDTO
import java.io.File

class AutomationTest : BaseReleaseManagementServiceFuncTest {

    override fun getObjectMapper() = TestUtil.mapper

    @Test
    fun testDependenciesStatus() {
        val expected = "ReleaseManagementService:1.0.1 BUILD [\n    dependency1:0.1 RC\n    dependency2:0.2 RELEASE\n]"
        val outFile = File("").resolve("build").resolve("automation-test")
            .resolve("testDependenciesStatus.txt").also { it.parentFile.mkdirs() }
        TestUtil.executeAutomation(
            DependenciesStatus.COMMAND,
            "${DependenciesStatus.COMPONENT_NAME}=ReleaseManagementService",
            "${DependenciesStatus.VERSION}=1.0.1",
            "${DependenciesStatus.DEPENDENCIES_STATUS_FILE}=${outFile.absolutePath}"
        )
        Assertions.assertEquals(expected, outFile.readText().replace("\r\n", "\n"))
    }

    @Test
    fun testMandatoryUpdate() {
        val outFile = File("").resolve("build").resolve("automation-test")
            .resolve("testMandatoryUpdate.json").also { it.parentFile.mkdirs() }
        val expected = getObjectMapper()
            .registerKotlinModule()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(
                loadObject("../test-data/releng/create-mandatory-update-3.json", object : TypeReference<MandatoryUpdateResponseDTO>() {})
            )
        TestUtil.executeAutomation(
            MandatoryUpdate.COMMAND,
            "${MandatoryUpdate.COMPONENT}=dependency-component-first",
            "${MandatoryUpdate.VERSION}=1.0.2",
            "${MandatoryUpdate.PROJECT_KEY}=PROJ",
            "${MandatoryUpdate.EPIC_NAME}=Mandatory update to dependency-component-first",
            "${MandatoryUpdate.DUE_DATE}=2025-08-15",
            "${MandatoryUpdate.CUSTOMER}=Octopus",
            "${MandatoryUpdate.DRY_RUN}=false",
            "${MandatoryUpdate.IS_FULL_MATCH_SYSTEMS}=true",
            "${MandatoryUpdate.ACTIVE_LINE_PERIOD}=180",
            "${MandatoryUpdate.OUTPUT_FILE}=${outFile.absolutePath}"
        )
        Assertions.assertEquals(expected, outFile.readText())
    }
}