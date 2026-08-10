package com.rezacah.ngaburake.plugin

import java.io.File

/** Parses R8/ProGuard `mapping.txt` files into a queryable [MappingIndex]. */
internal object MappingParser {

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
                val parts = line.split("->").map { it.trim().removeSuffix(":") }
                if (parts.size == 2) {
                    entries.add(MappingEntry(original = parts[0], obfuscated = parts[1]))
                }
            }
        }
        return MappingIndex(entries)
    }
}
