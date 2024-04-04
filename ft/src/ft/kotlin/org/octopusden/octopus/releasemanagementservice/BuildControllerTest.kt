package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.BuildFilterDTO
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ErrorResponse
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ReleaseManagementServiceErrorCode
import org.octopusden.octopus.releasemanagementservice.client.common.dto.ShortBuildDTO
import org.octopusden.octopus.releasemanagementservice.client.common.exception.NotFoundException
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider

class BuildControllerTest : BaseBuildControllerTest(), BaseReleaseManagementServiceFuncTest {

    override fun getObjectMapper(): ObjectMapper = mapper

    override fun getBuilds(component: String, params: Map<String, Any>): Collection<ShortBuildDTO> {
        return client.getBuilds(component, mapper.convertValue(params, BuildFilterDTO::class.java))
    }

    override fun getBuild(component: String, version: String): BuildDTO {
        return client.getBuild(component, version)
    }

    override fun getNotExistedBuildErrorResponse(component: String, version: String): ErrorResponse {
        try {
            client.getBuild(component, version)
            return ErrorResponse(ReleaseManagementServiceErrorCode.OTHER, "Failure expected")
        } catch (e: NotFoundException) {
            return ErrorResponse(ReleaseManagementServiceErrorCode.NOT_FOUND, e.message!!)
        }
    }

    companion object {
        @JvmStatic
        private val mapper = jacksonObjectMapper()

        @JvmStatic
        private val client =
            ClassicReleaseManagementServiceClient(object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = "http://localhost:8080"
                override fun getTimeRetryInMillis() = 180000
            })
    }
}
