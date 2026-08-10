package com.rezacah.ngaburake.report

/**
 * A single result of checking whether [target] was obfuscated as expected.
 *
 * @param target fully qualified class name (or member) that was checked
 * @param type what kind of target this is
 * @param severity how serious this finding is
 * @param detail human-readable explanation, e.g. the original/obfuscated name pair
 */
data class Finding(
    val target: String,
    val type: FindingType,
    val severity: Severity,
    val detail: String,
)
