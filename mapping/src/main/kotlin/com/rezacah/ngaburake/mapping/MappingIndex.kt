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
}
