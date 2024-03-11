package org.octopusden.octopus.releasemanagement

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class ReleaseManagementServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(ReleaseManagementServiceApplication::class.java, *args)
}
