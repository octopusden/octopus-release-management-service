package org.octopusden.octopus.releasemanagementservice

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.automation.releasemanagement.DependenciesStatus
import java.io.File

class AutomationTest {
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
}