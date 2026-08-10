package com.rezacah.ngaburake.plugin

/** Output format for a generated report. */
enum class ReportFormat {
    /** Minimal JSON array, schema: `target`, `type`, `severity`, `detail` per entry. */
    JSON,

    /**
     * Not implemented yet — planned for v1.1. Selecting this format causes `verifyObfuscation`
     * to throw an [IllegalStateException] at execution time.
     */
    HTML,

    /** Short pass/fail line per class, printed to the Gradle console log. */
    CONSOLE,
}
