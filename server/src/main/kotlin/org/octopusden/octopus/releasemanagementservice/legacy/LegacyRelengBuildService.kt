package org.octopusden.octopus.releasemanagementservice.legacy

import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException
import org.octopusden.octopus.releasemanagementservice.client.common.exception.ReleaseManagementServiceException
import org.octopusden.octopus.releasemanagementservice.service.BuildService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LegacyRelengBuildService(private val client: LegacyRelengClient) : BuildService {

    private val codeMapping = mapOf<Int, (e: LegacyRelengClientException) ->
    ReleaseManagementServiceException>(404 to { e: LegacyRelengClientException -> NotFoundException(e.message!!) })

    override fun getBuilds(component: String, filter: BuildFilterDTO): Collection<ShortBuildDTO> {
        return execute("getBuilds($component, $filter)") { client.getBuilds(component, filter) }
    }

    override fun getBuild(component: String, version: String): BuildDTO {
        return execute("getBuild($component, $version)") { client.getBuild(component, version) }
    }

    private fun <T : Any> execute(operationName: String, func: () -> T): T {
        return try {
            log.trace("Execute: '{}'", operationName)
            func()
        } catch (e: LegacyRelengClientException) {
            log.error(e.message)
            throw codeMapping[e.code]
                ?.invoke(e)
                ?: IllegalStateException(e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(LegacyRelengBuildService::class.java)
    }
}
