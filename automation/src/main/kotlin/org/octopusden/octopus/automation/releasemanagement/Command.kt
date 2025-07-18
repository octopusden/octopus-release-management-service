package org.octopusden.octopus.automation.releasemanagement

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider
import org.slf4j.LoggerFactory

class Command : CliktCommand(name = "") {
    private val url by option(URL_OPTION, help = "Release Management Service URL").convert { it.trim() }.required()
        .check("$URL_OPTION is empty") { it.isNotEmpty() }

    private val context by findOrSetObject { mutableMapOf<String, Any>() }

    override fun run() {
        val log = LoggerFactory.getLogger(Command::class.java.`package`.name)
        val client = ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
            override fun getApiUrl() = url
            override fun getTimeRetryInMillis() = 180000
        })

        context[LOG] = log
        context[CLIENT] = client
        log.debug("Releasing management URL: $url")
    }

    companion object {
        const val URL_OPTION = "--url"
        const val LOG = "log"
        const val CLIENT = "client"
    }
}