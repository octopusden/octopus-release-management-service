package org.octopusden.octopus.automation.releasemanagement.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.octopusden.octopus.automation.releasemanagement.helper.VelocityEngine
import org.slf4j.Logger

import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import java.io.File

class DependenciesStatus : CliktCommand(name = COMMAND) {
    private val context by requireObject<MutableMap<String, Any>>()

    private val componentName by option(COMPONENT_NAME, help = "Component name")
        .convert { it.trim() }.required()
        .check("$COMPONENT_NAME is empty") { it.isNotEmpty() }

    private val version by option(VERSION, help = "Component version")
        .convert { it.trim() }.required()
        .check("$VERSION is empty") { it.isNotEmpty() }

    private val file by option(DEPENDENCIES_STATUS_FILE, help = "File to save dependencies status")
        .convert { it.trim() }.required()
        .check("$DEPENDENCIES_STATUS_FILE must be a valid path") { it.isNotEmpty() }

    private val client by lazy { context[Command.CLIENT] as ClassicReleaseManagementServiceClient }
    private val log by lazy { context[Command.LOG] as Logger }

    override fun run() {
        log.info("Write Dependencies Status of '$componentName:$version' to '$file'")
        val build = client.getBuild(componentName, version)

        File(file).writeText(
            VelocityEngine().generate(
                mapOf("build" to build),
                "velocity-templates/text.vm"
            )
        )
    }

    companion object {
        const val COMMAND = "dependencies-status"
        const val COMPONENT_NAME = "--component"
        const val VERSION = "--version"
        const val DEPENDENCIES_STATUS_FILE = "--dependencies-status-file"
    }
}
