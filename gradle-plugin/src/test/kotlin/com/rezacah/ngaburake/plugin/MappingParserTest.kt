package com.rezacah.ngaburake.plugin

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class MappingParserTest {

    private fun mappingFile(content: String): File =
        File.createTempFile("mapping", ".txt").apply { writeText(content) }

    @Test
    fun `class yang ter-obfuscate terdeteksi sebagai Obfuscated`() {
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
    fun `class yang tidak ter-obfuscate karena keep rule terdeteksi sebagai NotObfuscated`() {
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
    fun `class yang tidak ada di mapping terdeteksi sebagai NotFoundInMapping`() {
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
    fun `baris member berindentasi tidak dianggap header class baru`() {
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
