package org.octopusden.octopus.automation.releasemanagement

import com.github.ajalt.clikt.core.subcommands
import org.octopusden.octopus.automation.releasemanagement.command.Command
import org.octopusden.octopus.automation.releasemanagement.command.DependenciesStatus
import org.octopusden.octopus.automation.releasemanagement.command.MandatoryUpdate

const val SPLIT_SYMBOLS = "[,;]"

fun main(args: Array<String>) {
    Command().subcommands(
        DependenciesStatus(),
        MandatoryUpdate()
        //TODO: More commands
    ).main(args)
}