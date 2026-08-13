package com.rezacah.ngaburake.mapping

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class MappingParserTest {

    private fun mappingFile(content: String): File =
        File.createTempFile("mapping", ".txt").apply { writeText(content) }

    @Test
    fun `obfuscated class is detected as Obfuscated`() {
        val file = mappingFile(
            """
            com.rezacah.ngaburake.PaymentManager -> a.b.c:
                java.lang.String apiKey -> a
            """.trimIndent(),
        )

        val index = MappingParser.parse(file)

        assertEquals(
            ObfuscationCheckResult.Obfuscated,
            index.check("com.rezacah.ngaburake.PaymentManager"),
        )
    }

    @Test
    fun `class kept by a keep rule is detected as NotObfuscated`() {
        val file = mappingFile(
            """
            com.rezacah.ngaburake.PaymentManager -> com.rezacah.ngaburake.PaymentManager:
            """.trimIndent(),
        )

        val index = MappingParser.parse(file)

        assertEquals(
            ObfuscationCheckResult.NotObfuscated,
            index.check("com.rezacah.ngaburake.PaymentManager"),
        )
    }

    @Test
    fun `class absent from mapping is detected as NotFoundInMapping`() {
        val file = mappingFile(
            """
            com.rezacah.ngaburake.OtherClass -> a:
            """.trimIndent(),
        )

        val index = MappingParser.parse(file)

        assertEquals(
            ObfuscationCheckResult.NotFoundInMapping,
            index.check("com.rezacah.ngaburake.PaymentManager"),
        )
    }

    @Test
    fun `header line with multiple arrows on the obfuscated side is not silently dropped`() {
        // Not realistic for real R8 output (FQCNs can't contain "->"), but a hand-edited or
        // corrupted mapping file could have this shape — split(limit = 2) must still parse it
        // instead of silently skipping the line.
        val file = mappingFile(
            """
            com.rezacah.ngaburake.PaymentManager -> a -> b:
            """.trimIndent(),
        )

        val index = MappingParser.parse(file)

        assertEquals(
            ObfuscationCheckResult.Obfuscated,
            index.check("com.rezacah.ngaburake.PaymentManager"),
        )
    }

    @Test
    fun `indented member line is not treated as a new class header`() {
        val file = mappingFile(
            """
            com.rezacah.ngaburake.PaymentManager -> a:
                void processPayment(java.lang.String) -> a
            """.trimIndent(),
        )

        val index = MappingParser.parse(file)

        assertEquals(
            ObfuscationCheckResult.NotFoundInMapping,
            index.check("processPayment"),
        )
    }
}
