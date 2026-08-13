package com.rezacah.ngaburake.report

/** Output format for a generated report. */
enum class ReportFormat {
    /** Minimal JSON array, schema: `target`, `type`, `severity`, `detail` per entry. */
    JSON,

    /** Standalone HTML page with a color-coded table, one row per finding. */
    HTML,

    /** Short pass/fail line per class, printed to the Gradle console log. */
    CONSOLE,

    /**
     * [SARIF 2.1.0](https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html) — for
     * upload to GitHub/GitLab code scanning (e.g. `github/codeql-action/upload-sarif`). Findings
     * with a resolved source file get a `physicalLocation`; findings without one (e.g. anything
     * from the runtime SDK, which has no access to source at all) fall back to a
     * `logicalLocation` keyed by the fully qualified class name.
     */
    SARIF,
}
