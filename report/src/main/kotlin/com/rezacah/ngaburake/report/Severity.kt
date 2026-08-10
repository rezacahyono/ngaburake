package com.rezacah.ngaburake.report

/**
 * How serious a single obfuscation [Finding] is.
 */
enum class Severity {
    /** Class is verified obfuscated, no action needed. */
    OK,

    /** Class could not be found in the mapping file — not verified, not necessarily a failure. */
    WARNING,

    /** Class was found in the mapping file but was not renamed — a real obfuscation gap. */
    CRITICAL,
}
