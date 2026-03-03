package org.octopusden.octopus.releasemanagementservice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
@ConfigurationPropertiesScan
class ReleaseManagementServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(ReleaseManagementServiceApplication::class.java, *args)
}
