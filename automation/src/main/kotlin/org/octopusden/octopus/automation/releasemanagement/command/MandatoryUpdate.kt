package org.octopusden.octopus.automation.releasemanagement.command

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.octopusden.octopus.automation.releasemanagement.SPLIT_SYMBOLS
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.MandatoryUpdateFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.slf4j.Logger
import java.io.File
import java.text.SimpleDateFormat

class MandatoryUpdate : CliktCommand(name = COMMAND) {
    private val context by requireObject<MutableMap<String, Any>>()
    private val client by lazy { context[Command.CLIENT] as ClassicReleaseManagementServiceClient }
    private val log by lazy { context[Command.LOG] as Logger }

    private val component by option(COMPONENT, help = "Component name")
        .convert { it.trim() }.required()
        .check("$COMPONENT must not be empty") { it.isNotEmpty() }

    private val version by option(VERSION, help = "Component version")
        .convert { it.trim() }.required()
        .check("$VERSION must not be empty") { it.isNotEmpty() }

    private val projectKey by option(PROJECT_KEY, help = "Project key in which the epic will be created")
        .convert { it.trim() }.required()
        .check("$PROJECT_KEY must not be empty") { it.isNotEmpty() }

    private val epicName by option(EPIC_NAME, help = "Epic name")
        .convert { it.trim() }.required()
        .check("$EPIC_NAME must not be empty") { it.isNotEmpty() }

    private val dueDate by option(DUE_DATE, help = "Due date (yyyy-MM-dd)")
        .convert {
            try {
                SimpleDateFormat("yyyy-MM-dd").parse(it)
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid date format for $DUE_DATE: $it (expected yyyy-MM-dd)")
            }
        }
        .required()

    private val customer by option(CUSTOMER, help = "Customer")
        .convert { it.trim() }.required()
        .check("$CUSTOMER must not be empty") { it.isNotEmpty() }

    private val notice by option("--notice", help = "Additional notice")
        .convert { it.trim() }
        .default("")

    private val activeLinePeriod by option(ACTIVE_LINE_PERIOD, help = "Active line period (days)")
        .convert { it.toInt() }
        .default(0)

    private val excludeComponents by option(EXCLUDE_COMPONENTS, help = "Exclude components (comma-separated)")
        .convert {
            it.split(Regex(SPLIT_SYMBOLS))
                .map(String::trim)
                .filter(String::isNotEmpty)
                .toSet()
        }
        .default(emptySet())

    private val systems by option(SYSTEMS, help = "Systems with which components will be excluded (comma-separated)")
        .convert {
            it.split(Regex(SPLIT_SYMBOLS))
                .map(String::trim)
                .filter(String::isNotEmpty)
                .toSet()
        }
        .default(emptySet())

    private val outputFile by option(OUTPUT_FILE, help = "File to save result")
        .convert { it.trim() }
        .default("")

    private val dryRun by option(DRY_RUN, help = "Dry run only, do not apply")
        .convert { it.toBoolean() }
        .default(true)

    override fun run() {
        val filter = MandatoryUpdateFilterDTO(
            activeLinePeriod = activeLinePeriod,
            excludeComponents = excludeComponents,
            systems = systems
        )
        val dto = MandatoryUpdateDTO(
            component = component,
            version = version,
            projectKey = projectKey,
            epicName = epicName,
            dueDate = dueDate,
            notice = notice,
            customer = customer,
            filter = filter
        )
        log.info("Executing mandatory update: dto={} dryRun={}", dto, dryRun)
        val result = jacksonObjectMapper()
            .registerKotlinModule()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .writeValueAsString(client.createMandatoryUpdate(dryRun, dto))
        File(outputFile).writeText(result)
        log.info("Result is written to file $outputFile")
    }

    companion object {
        const val COMMAND = "mandatory-update"
        const val COMPONENT = "--component"
        const val VERSION = "--version"
        const val PROJECT_KEY = "--project-key"
        const val EPIC_NAME = "--epic-name"
        const val DUE_DATE = "--due-date"
        const val CUSTOMER = "--customer"
        const val ACTIVE_LINE_PERIOD = "--active-line-period"
        const val EXCLUDE_COMPONENTS = "--exclude-components"
        const val SYSTEMS = "--systems"
        const val OUTPUT_FILE = "--output-file"
        const val DRY_RUN = "--dry-run"
    }
}