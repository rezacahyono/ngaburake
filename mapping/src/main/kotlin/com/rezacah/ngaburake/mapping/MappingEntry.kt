package com.rezacah.ngaburake.mapping

/** One `original -> obfuscated:` header line from an R8 `mapping.txt` file. */
data class MappingEntry(
    val original: String,
    val obfuscated: String,
)
