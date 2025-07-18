package org.octopusden.octopus.automation.releasemanagement

import com.github.ajalt.clikt.core.subcommands

const val SPLIT_SYMBOLS = "[,;]"

fun main(args: Array<String>) {
    Command().subcommands(
        DependenciesStatus(),
        //TODO: More commands
    ).main(args)
}