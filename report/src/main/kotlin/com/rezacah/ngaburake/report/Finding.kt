package com.rezacah.ngaburake.report

/**
 * A single result of checking whether [target] was obfuscated as expected.
 *
 * @param target fully qualified class name (or member) that was checked
 * @param type what kind of target this is
 * @param severity how serious this finding is
 * @param detail human-readable explanation, e.g. the original/obfuscated name pair
 * @param filePath source file path relative to a project source root, if it could be resolved
 *   (build-time only — the runtime SDK has no access to source and always leaves this `null`).
 *   Used by [ReportFormat.SARIF] to emit a `physicalLocation`; other formats ignore it.
 * @param line best-effort line number when [filePath] is known — not derived from actual
 *   bytecode debug info, so it should be treated as "somewhere in this file", not a precise
 *   pointer. `null` when [filePath] is `null`.
 */
data class Finding(
    val target: String,
    val type: FindingType,
    val severity: Severity,
    val detail: String,
    val filePath: String? = null,
    val line: Int? = null,
)
