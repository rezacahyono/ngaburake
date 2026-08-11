package com.rezacah.ngaburake.runtime

import com.rezacah.ngaburake.mapping.MappingIndex
import com.rezacah.ngaburake.mapping.ObfuscationCheckResult
import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.Severity

/**
 * Checks whether a class's *name* was obfuscated, using live reflection against the running app.
 *
 * When [mappingIndex] is supplied, it's checked first — an exact answer from the R8 mapping file
 * is more reliable than the [isNameShortened] heuristic below (e.g. it wouldn't be fooled by a
 * custom dictionary obfuscator that renames to another readable-looking word). If [mappingIndex]
 * is `null`, or the class isn't in it, this falls back to the heuristic.
 *
 * @param mappingIndex optional index built from a mapping file the caller supplied (see
 *   `ObfuscationSDK.Builder.withMappingFile`, added in a later iteration) — `mapping.txt` is a
 *   build artifact, not bundled into the APK by default, so this is opt-in.
 */
class ClassNameChecker(private val mappingIndex: MappingIndex? = null) : ObfuscationChecker {

    override fun check(className: String): Finding {
        mappingIndex?.let { index ->
            return when (index.check(className)) {
                ObfuscationCheckResult.Obfuscated -> ok(className, "renamed as expected (verified via mapping file)")
                ObfuscationCheckResult.NotObfuscated -> critical(
                    className,
                    "kept its original name (verified via mapping file) — check for an overly broad -keep rule",
                )
                ObfuscationCheckResult.NotFoundInMapping -> checkByReflection(className)
            }
        }
        return checkByReflection(className)
    }

    private fun checkByReflection(className: String): Finding =
        if (isNameShortened(className)) {
            ok(className, "class name appears obfuscated (short, non-descriptive simple name)")
        } else {
            warning(
                className,
                "class name does not look obfuscated — this is a heuristic guess, not a confirmed " +
                    "violation; a custom dictionary obfuscator could produce a false positive here",
            )
        }

    /**
     * Best-effort heuristic: R8's default renaming produces very short, non-word simple names
     * (`a`, `b`, `aa`). Does not catch custom dictionary obfuscation that renames to another
     * readable-looking word of similar length — see `docs/TECHNICAL.md` §4.3 for the known
     * limitation this heuristic has.
     */
    private fun isNameShortened(fqcn: String): Boolean =
        try {
            val simpleName = Class.forName(fqcn).simpleName
            simpleName.length <= 2 && !simpleName.contains(Regex("[A-Z][a-z]+"))
        } catch (e: ClassNotFoundException) {
            // Class doesn't exist at all under this name at runtime — could mean it was
            // obfuscated to something we can't look up by the original name, or it was
            // stripped/inlined entirely. Treat as "looks obfuscated" rather than silently
            // failing the check.
            true
        }

    private fun ok(target: String, detail: String) =
        Finding(target = target, type = FindingType.CLASS_NAME, severity = Severity.OK, detail = detail)

    private fun critical(target: String, detail: String) =
        Finding(target = target, type = FindingType.CLASS_NAME, severity = Severity.CRITICAL, detail = detail)

    private fun warning(target: String, detail: String) =
        Finding(target = target, type = FindingType.CLASS_NAME, severity = Severity.WARNING, detail = detail)
}
