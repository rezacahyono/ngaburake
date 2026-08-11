package com.rezacah.ngaburake.report

/** Output format for a generated report. */
enum class ReportFormat {
    /** Minimal JSON array, schema: `target`, `type`, `severity`, `detail` per entry. */
    JSON,

    /** Standalone HTML page with a color-coded table, one row per finding. */
    HTML,

    /** Short pass/fail line per class, printed to the Gradle console log. */
    CONSOLE,
}
