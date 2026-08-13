package com.rezacah.ngaburake.report

import kotlinx.serialization.json.Json

/**
 * Renders findings as a [SARIF 2.1.0](https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html)
 * log, for upload to GitHub/GitLab code scanning.
 *
 * Every [Severity] is included (`OK` maps to `note`, not excluded) — the report is meant to show
 * a complete picture of what was checked, not just violations.
 */
internal class SarifReportFormatter : ReportFormatter {

    // encodeDefaults: SarifLog.schema/version and SarifDriver.name/informationUri all have
    // default values (they're fixed for this tool) — kotlinx.serialization skips default values
    // by default, which would silently drop $schema/version from the output entirely.
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    override fun format(findings: List<Finding>): String {
        val rules = findings.map { it.type }.distinct().map { type -> ruleFor(type) }
        val results = findings.map { finding -> resultFor(finding) }

        val log = SarifLog(
            runs = listOf(
                SarifRun(
                    tool = SarifTool(driver = SarifDriver(rules = rules)),
                    results = results,
                ),
            ),
        )
        return json.encodeToString(SarifLog.serializer(), log)
    }

    private fun ruleFor(type: FindingType): SarifRule {
        val id = ruleId(type)
        return SarifRule(
            id = id,
            name = id,
            shortDescription = SarifMessage(text = "Obfuscation check: ${type.name.lowercase()}"),
        )
    }

    private fun resultFor(finding: Finding): SarifResult = SarifResult(
        ruleId = ruleId(finding.type),
        level = levelFor(finding.severity),
        message = SarifMessage(text = finding.detail),
        locations = listOf(locationFor(finding)),
    )

    private fun locationFor(finding: Finding): SarifLocation {
        val filePath = finding.filePath
        return if (filePath != null) {
            SarifLocation(
                physicalLocation = SarifPhysicalLocation(
                    artifactLocation = SarifArtifactLocation(uri = filePath),
                    region = SarifRegion(startLine = finding.line ?: 1),
                ),
            )
        } else {
            SarifLocation(
                logicalLocations = listOf(SarifLogicalLocation(fullyQualifiedName = finding.target)),
            )
        }
    }

    private fun ruleId(type: FindingType): String = when (type) {
        FindingType.CLASS_NAME -> "obfuscation-class-name"
        FindingType.FIELD_NAME -> "obfuscation-field-name"
        FindingType.METHOD_NAME -> "obfuscation-method-name"
    }

    private fun levelFor(severity: Severity): String = when (severity) {
        Severity.OK -> "note"
        Severity.WARNING -> "warning"
        Severity.CRITICAL -> "error"
    }
}
