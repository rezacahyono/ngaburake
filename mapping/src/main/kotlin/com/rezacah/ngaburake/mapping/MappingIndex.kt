package com.rezacah.ngaburake.mapping

/** In-memory, queryable index over the class headers parsed from an R8 `mapping.txt` file. */
class MappingIndex(private val entries: List<MappingEntry>) {

    private val byOriginalName: Map<String, MappingEntry> = entries.associateBy { it.original }

    /**
     * Checks whether [className] was obfuscated according to this mapping.
     *
     * @return [ObfuscationCheckResult.NotFoundInMapping] if [className] has no entry at all —
     *   callers must not treat this the same as [ObfuscationCheckResult.NotObfuscated].
     */
    fun check(className: String): ObfuscationCheckResult {
        val entry = byOriginalName[className] ?: return ObfuscationCheckResult.NotFoundInMapping
        return if (entry.original != entry.obfuscated) {
            ObfuscationCheckResult.Obfuscated
        } else {
            ObfuscationCheckResult.NotObfuscated
        }
    }

    /**
     * Every original class name in this mapping that belongs to [packagePrefix] — used to expand
     * a `"com.foo.payment.*"`-style wildcard entry into concrete class names.
     *
     * Boundary-aware: `"com.foo.payment"` matches `"com.foo.payment.Foo"` but not
     * `"com.foo.paymentXyz.Foo"` — the prefix is matched against `"$packagePrefix."`, not a plain
     * substring. Result is sorted alphabetically for deterministic report output.
     */
    fun classesUnder(packagePrefix: String): List<String> =
        byOriginalName.keys.filter { it.startsWith("$packagePrefix.") }.sorted()
}
