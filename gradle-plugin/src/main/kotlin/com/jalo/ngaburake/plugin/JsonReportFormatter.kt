package com.jalo.ngaburake.plugin

/**
 * Renders findings as a minimal JSON array, schema: `target`, `type`, `severity`, `detail` per
 * entry. Hand-rolled (no JSON library dependency) since the schema is small and fixed for MVP.
 */
internal class JsonReportFormatter : ReportFormatter {
    override fun format(findings: List<Finding>): String {
        val items = findings.joinToString(",\n") { finding ->
            """
            |  {
            |    "target": "${finding.target.escape()}",
            |    "type": "${finding.type}",
            |    "severity": "${finding.severity}",
            |    "detail": "${finding.detail.escape()}"
            |  }
            """.trimMargin()
        }
        return "[\n$items\n]"
    }

    private fun String.escape(): String = replace("\\", "\\\\").replace("\"", "\\\"")
}
