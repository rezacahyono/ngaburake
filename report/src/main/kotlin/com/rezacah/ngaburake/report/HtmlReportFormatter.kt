package com.rezacah.ngaburake.report

/** Renders findings as a simple standalone HTML table — one row per class, color-coded by severity. */
internal class HtmlReportFormatter : ReportFormatter {
    override fun format(findings: List<Finding>): String {
        val rows = findings.joinToString("\n") { finding ->
            """
            |    <tr class="${finding.severity.name.lowercase()}">
            |      <td>${finding.target.escapeHtml()}</td>
            |      <td>${finding.type}</td>
            |      <td>${finding.severity}</td>
            |      <td>${finding.detail.escapeHtml()}</td>
            |    </tr>
            """.trimMargin()
        }
        return """
        |<!DOCTYPE html>
        |<html>
        |<head>
        |  <meta charset="utf-8">
        |  <title>Obfuscation Verification Report</title>
        |  <style>
        |    body { font-family: sans-serif; }
        |    table { border-collapse: collapse; width: 100%; }
        |    th, td { border: 1px solid #ccc; padding: 6px 10px; text-align: left; }
        |    tr.ok { background-color: #e6ffe6; }
        |    tr.warning { background-color: #fff8e1; }
        |    tr.critical { background-color: #ffe6e6; }
        |  </style>
        |</head>
        |<body>
        |  <h1>Obfuscation Verification Report</h1>
        |  <table>
        |    <tr><th>Target</th><th>Type</th><th>Severity</th><th>Detail</th></tr>
        |$rows
        |  </table>
        |</body>
        |</html>
        """.trimMargin()
    }

    private fun String.escapeHtml(): String =
        replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
