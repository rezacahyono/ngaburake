package com.rezacah.ngaburake.plugin

import java.io.File

/**
 * Best-effort lookup of a class's source file among a fixed set of [sourceRoots] — a plain
 * `.`-to-`/` + `.kt`/`.java` filename match, not a real compiler symbol lookup.
 *
 * There is no bytecode debug info (line-number table) available here, so a resolved location is
 * always file-level: "somewhere in this file", not a specific line. Callers should treat the
 * line as a starting point for manual investigation, not a precise pointer.
 */
internal class SourceLocationResolver(private val sourceRoots: List<File>) {

    /**
     * Returns the absolute source file matching [className], or `null` if no `.kt`/`.java` file
     * matching that name exists under any configured root.
     */
    fun resolve(className: String): File? {
        val relativePath = className.replace('.', '/')
        for (root in sourceRoots) {
            for (extension in listOf("kt", "java")) {
                val candidate = File(root, "$relativePath.$extension")
                if (candidate.isFile) return candidate
            }
        }
        return null
    }
}
