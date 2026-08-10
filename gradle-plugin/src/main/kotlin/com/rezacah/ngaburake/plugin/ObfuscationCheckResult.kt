package com.rezacah.ngaburake.plugin

/** Outcome of checking a single class name against a [MappingIndex]. */
internal sealed interface ObfuscationCheckResult {
    /** Class was found in the mapping and its name was actually renamed. */
    data object Obfuscated : ObfuscationCheckResult

    /** Class was found in the mapping but kept its original name (e.g. matched a `-keep` rule). */
    data object NotObfuscated : ObfuscationCheckResult

    /** Class does not appear in the mapping at all — not verified, could be a typo or a missing compile. */
    data object NotFoundInMapping : ObfuscationCheckResult
}
