package com.jalo.ngaburake.plugin

/**
 * How serious a single obfuscation [Finding] is.
 */
internal enum class Severity {
    /** Class is verified obfuscated, no action needed. */
    OK,

    /** Class could not be found in the mapping file — not verified, not necessarily a failure. */
    WARNING,

    /** Class was found in the mapping file but was not renamed — a real obfuscation gap. */
    CRITICAL,
}
