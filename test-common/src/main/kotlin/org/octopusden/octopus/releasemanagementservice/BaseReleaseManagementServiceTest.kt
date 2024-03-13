package org.octopusden.octopus.releasemanagementservice

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
interface BaseReleaseManagementServiceTest {

    fun getObjectMapper(): ObjectMapper

    fun <T> loadObject(file: String, typeReference: TypeReference<T>): T =
        getObjectMapper().readValue(File(file), typeReference)
}
