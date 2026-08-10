package com.rezacah.ngaburake.plugin

/** One `original -> obfuscated:` header line from an R8 `mapping.txt` file. */
internal data class MappingEntry(
    val original: String,
    val obfuscated: String,
)
