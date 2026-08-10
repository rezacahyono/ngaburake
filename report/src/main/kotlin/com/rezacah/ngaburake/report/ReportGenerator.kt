package com.rezacah.ngaburake.report

/**
 * Single convergence point for turning a list of [Finding]s into an output string.
 *
 * Picks a [ReportFormatter] based on [format] (Factory pattern) — adding a new output format
 * later means adding a new [ReportFormatter] implementation, not changing callers.
 */
object ReportGenerator {
    fun generate(findings: List<Finding>, format: ReportFormat): String {
        val formatter: ReportFormatter = when (format) {
            ReportFormat.CONSOLE -> ConsoleReportFormatter()
            ReportFormat.JSON -> JsonReportFormatter()
            ReportFormat.HTML -> error("HTML report format is not implemented yet (planned for v1.1).")
        }
        return formatter.format(findings)
    }
}
