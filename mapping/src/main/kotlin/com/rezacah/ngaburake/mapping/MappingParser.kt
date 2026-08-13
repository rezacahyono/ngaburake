package com.rezacah.ngaburake.mapping

import java.io.File

/** Parses R8/ProGuard `mapping.txt` files into a queryable [MappingIndex]. */
object MappingParser {

    /**
     * Reads [mappingFile] and indexes every class header line (`original -> obfuscated:`).
     *
     * Indented lines (member mappings — fields/methods) are skipped; they are not needed until
     * the runtime `ReflectionChecker` cross-check planned for v1.1.
     */
    fun parse(mappingFile: File): MappingIndex {
        val entries = mutableListOf<MappingEntry>()
        mappingFile.forEachLine { line ->
            if (!line.startsWith(" ") && line.contains("->")) {
                // limit = 2: only the first "->" is the header separator. A FQCN can never
                // literally contain "->" (not a valid identifier character), but splitting
                // without a limit would still silently drop a line if the obfuscated side ever
                // did (e.g. a hand-edited or corrupted mapping file) — split(limit = 2) makes
                // parts.size == 2 guaranteed whenever contains("->") is true, so there's no
                // silent-drop case left to guard against.
                val (original, obfuscated) = line.split("->", limit = 2)
                    .map { it.trim().removeSuffix(":") }
                entries.add(MappingEntry(original = original, obfuscated = obfuscated))
            }
        }
        return MappingIndex(entries)
    }
}
