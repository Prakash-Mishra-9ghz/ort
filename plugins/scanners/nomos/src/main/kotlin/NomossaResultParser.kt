package org.ossreviewtoolkit.plugins.scanners.nomos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.databind.DeserializationFeature

data class NomossaResult(
    val results: List<NomossaFileResult>
)

data class NomossaFileResult(
    val file: String,
    val licenses: List<String>
)

private val nomossaMapper: ObjectMapper = jacksonObjectMapper().apply {
    findAndRegisterModules()
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

/**
 * Parses the JSON result string returned by Nomossa into a [NomossaResult] object.
 */
fun parseNomossaResult(result: String): NomossaResult =
    nomossaMapper.readValue(result)
