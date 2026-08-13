package com.rezacah.ngaburake.testing

import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.Severity
import com.rezacah.ngaburake.runtime.ObfuscationChecker

/**
 * Deterministic [ObfuscationChecker] for consumers to use in their own unit tests — lets you
 * unit test code that calls into the SDK without triggering real reflection or I/O.
 *
 * By default, returns [Severity.OK] for every class checked. Configure [findingsByClassName] to
 * return specific findings per class, or [defaultSeverity] to change the fallback for classes not
 * explicitly configured. [type] defaults to [FindingType.CLASS_NAME] — override it to simulate a
 * [FindingType.FIELD_NAME]/[FindingType.METHOD_NAME] finding, e.g. when testing code that
 * branches on `finding.type`, not just `finding.severity`.
 *
 * Example:
 * ```
 * val fake = FakeObfuscationChecker(
 *     findingsByClassName = mapOf(
 *         "com.example.PaymentManager" to Severity.CRITICAL,
 *     ),
 * )
 * ```
 */
class FakeObfuscationChecker(
    private val findingsByClassName: Map<String, Severity> = emptyMap(),
    private val defaultSeverity: Severity = Severity.OK,
    private val type: FindingType = FindingType.CLASS_NAME,
) : ObfuscationChecker {

    override fun check(className: String): Finding {
        val severity = findingsByClassName[className] ?: defaultSeverity
        return Finding(
            target = className,
            type = type,
            severity = severity,
            detail = "fake result configured for test",
        )
    }
}
