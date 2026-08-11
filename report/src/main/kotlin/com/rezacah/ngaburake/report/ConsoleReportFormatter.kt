package com.rezacah.ngaburake.report

/** Renders findings as a short pass/fail line per class, for Gradle console output. */
internal class ConsoleReportFormatter : ReportFormatter {
    override fun format(findings: List<Finding>): String {
        if (findings.isEmpty()) return "No sensitive classes configured."
        return findings.joinToString("\n") { finding ->
            val marker = when (finding.severity) {
                Severity.OK -> "PASS"
                Severity.WARNING -> "WARN"
                Severity.CRITICAL -> "FAIL"
            }
            "[$marker] ${finding.target} — ${finding.detail}"
        }
    }
}
