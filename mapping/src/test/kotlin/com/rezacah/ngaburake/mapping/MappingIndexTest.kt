package com.rezacah.ngaburake.mapping

import org.junit.Assert.assertEquals
import org.junit.Test

class MappingIndexTest {

    private fun indexOf(vararg originals: String): MappingIndex =
        MappingIndex(originals.map { MappingEntry(original = it, obfuscated = "x") })

    @Test
    fun `classesUnder returns every class matching the package prefix, sorted`() {
        val index = indexOf(
            "com.foo.payment.Zeta",
            "com.foo.payment.Alpha",
            "com.foo.other.Beta",
        )

        assertEquals(
            listOf("com.foo.payment.Alpha", "com.foo.payment.Zeta"),
            index.classesUnder("com.foo.payment"),
        )
    }

    @Test
    fun `classesUnder does not match a sibling package with a similar name`() {
        val index = indexOf("com.foo.paymentXyz.Foo")

        assertEquals(emptyList<String>(), index.classesUnder("com.foo.payment"))
    }

    @Test
    fun `classesUnder returns an empty list when nothing matches`() {
        val index = indexOf("com.foo.other.Beta")

        assertEquals(emptyList<String>(), index.classesUnder("com.foo.payment"))
    }
}
