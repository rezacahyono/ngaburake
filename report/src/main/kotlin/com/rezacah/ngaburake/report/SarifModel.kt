package com.rezacah.ngaburake.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minimal subset of the [SARIF 2.1.0](https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html)
 * schema needed to represent a list of [Finding]s — not a general-purpose SARIF model.
 */
@Serializable
internal data class SarifLog(
    @SerialName("\$schema")
    val schema: String =
        "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
    val version: String = "2.1.0",
    val runs: List<SarifRun>,
)

@Serializable
internal data class SarifRun(
    val tool: SarifTool,
    val results: List<SarifResult>,
)

@Serializable
internal data class SarifTool(
    val driver: SarifDriver,
)

@Serializable
internal data class SarifDriver(
    val name: String = "ngaburake",
    val informationUri: String = "https://github.com/rezacahyono/ngaburake",
    val rules: List<SarifRule>,
)

@Serializable
internal data class SarifRule(
    val id: String,
    val name: String,
    val shortDescription: SarifMessage,
)

@Serializable
internal data class SarifResult(
    val ruleId: String,
    val level: String,
    val message: SarifMessage,
    val locations: List<SarifLocation>,
)

@Serializable
internal data class SarifMessage(
    val text: String,
)

@Serializable
internal data class SarifLocation(
    val physicalLocation: SarifPhysicalLocation? = null,
    val logicalLocations: List<SarifLogicalLocation>? = null,
)

@Serializable
internal data class SarifPhysicalLocation(
    val artifactLocation: SarifArtifactLocation,
    val region: SarifRegion,
)

@Serializable
internal data class SarifArtifactLocation(
    val uri: String,
)

@Serializable
internal data class SarifRegion(
    val startLine: Int,
)

@Serializable
internal data class SarifLogicalLocation(
    val fullyQualifiedName: String,
)
