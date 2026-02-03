package org.octopusden.octopus.releasemanagementservice.actuator

import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClient
import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator
import org.springframework.boot.actuate.health.Health.Builder
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC
import org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC
import org.springframework.stereotype.Component

@Component
class LegacyRelengIndicator(
    private val legacyRelengClient: LegacyRelengClient, availability: ApplicationAvailability?
): ReadinessStateHealthIndicator(availability) {
    override fun doHealthCheck(builder: Builder) {
        try {
            legacyRelengClient.getHealth()
            builder
                .up()
                .withDetail("legacyReleng", "Available")
                .withDetail("readinessState", ACCEPTING_TRAFFIC)

        } catch (e: Exception) {
            builder
                .down()
                .withDetail("legacyReleng", "Unavailable")
                .withException(e)
                .withDetail("readinessState", REFUSING_TRAFFIC)
        }
    }
}