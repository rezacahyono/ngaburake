package com.rezacah.ngaburake.runtime

import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.Severity

/**
 * Checks whether any field or method on [className] still has a *meaningful* name containing a
 * sensitive keyword (`apiKey`, `secret`, `token`, `password`) — this can leak information via
 * reflection even when the class itself was renamed.
 *
 * Only the class's *own* declared members are inspected (not inherited ones), since obfuscation
 * decisions are per-class and an inherited member belongs to a different class's mapping entry.
 *
 * @param sensitiveKeywords case-insensitive substrings to flag in member names; defaults to a
 *   common set but callers can extend or replace it for domain-specific naming conventions.
 */
class ReflectionChecker(
    private val sensitiveKeywords: List<String> = DEFAULT_KEYWORDS,
) : ObfuscationChecker {

    override fun check(className: String): Finding {
        val clazz = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            return Finding(
                target = className,
                type = FindingType.FIELD_NAME,
                severity = Severity.WARNING,
                detail = "class not found at runtime — not verified",
            )
        }

        val leakyMember = (clazz.declaredFields.asSequence().map { it.name to FindingType.FIELD_NAME } +
            clazz.declaredMethods.asSequence().map { it.name to FindingType.METHOD_NAME })
            .firstOrNull { (name, _) -> containsSensitiveKeyword(name) }

        return if (leakyMember != null) {
            val (memberName, type) = leakyMember
            Finding(
                target = "$className.$memberName",
                type = type,
                severity = Severity.CRITICAL,
                detail = "member name '$memberName' still contains a sensitive keyword — leaks " +
                    "information via reflection even though the class itself may be renamed",
            )
        } else {
            Finding(
                target = className,
                type = FindingType.FIELD_NAME,
                severity = Severity.OK,
                detail = "no field or method name contains a configured sensitive keyword",
            )
        }
    }

    private fun containsSensitiveKeyword(memberName: String): Boolean =
        sensitiveKeywords.any { keyword -> memberName.contains(keyword, ignoreCase = true) }

    companion object {
        internal val DEFAULT_KEYWORDS = listOf("apiKey", "secret", "token", "password")
    }
}
