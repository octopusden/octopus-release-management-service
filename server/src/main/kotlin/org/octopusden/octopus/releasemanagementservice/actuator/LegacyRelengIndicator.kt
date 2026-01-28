package org.octopusden.octopus.releasemanagementservice.actuator

import org.octopusden.octopus.releasemanagementservice.legacy.LegacyRelengClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class LegacyRelengIndicator(
    private val legacyRelengClient: LegacyRelengClient
): HealthIndicator {
    override fun health(): Health {
        return try {
            legacyRelengClient.getHealth()
            Health.up().build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("message", "Legacy Releng Service is unavailable")
                .withException(e)
                .build()
        }
    }
}