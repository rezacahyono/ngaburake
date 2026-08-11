package com.rezacah.ngaburake.runtime

import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.Severity

/**
 * Aggregate result of an [ObfuscationSDK.verify] run.
 *
 * @param isObfuscated `true` only if every finding has severity [Severity.OK] — a single
 *   [Severity.WARNING] (not verified) or [Severity.CRITICAL] (confirmed violation) makes this
 *   `false`.
 * @param findings every individual check result, one per class/checker combination.
 */
data class ObfuscationResult(
    val isObfuscated: Boolean,
    val findings: List<Finding>,
) {
    companion object {
        internal fun from(findings: List<Finding>): ObfuscationResult =
            ObfuscationResult(
                isObfuscated = findings.all { it.severity == Severity.OK },
                findings = findings,
            )
    }
}
