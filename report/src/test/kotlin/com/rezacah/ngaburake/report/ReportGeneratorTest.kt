package com.rezacah.ngaburake.report

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportGeneratorTest {

    private val findings = listOf(
        Finding(
            target = "com.rezacah.ngaburake.PaymentManager",
            type = FindingType.CLASS_NAME,
            severity = Severity.CRITICAL,
            detail = "not renamed",
        ),
        Finding(
            target = "com.rezacah.ngaburake.ApiKeyStore",
            type = FindingType.CLASS_NAME,
            severity = Severity.OK,
            detail = "renamed to a.b",
        ),
    )

    @Test
    fun `console format berisi satu baris pass-fail per finding`() {
        val output = ReportGenerator.generate(findings, ReportFormat.CONSOLE)

        assertTrue(output.contains("[FAIL] com.rezacah.ngaburake.PaymentManager"))
        assertTrue(output.contains("[PASS] com.rezacah.ngaburake.ApiKeyStore"))
    }

    @Test
    fun `console format untuk list kosong menampilkan pesan info`() {
        val output = ReportGenerator.generate(emptyList(), ReportFormat.CONSOLE)

        assertEquals("No sensitive classes configured.", output)
    }

    @Test
    fun `json format berisi schema minimal target type severity detail`() {
        val output = ReportGenerator.generate(findings, ReportFormat.JSON)

        assertTrue(output.contains("\"target\": \"com.rezacah.ngaburake.PaymentManager\""))
        assertTrue(output.contains("\"type\": \"CLASS_NAME\""))
        assertTrue(output.contains("\"severity\": \"CRITICAL\""))
        assertTrue(output.contains("\"detail\": \"not renamed\""))
    }

    @Test
    fun `json format meng-escape tanda kutip di detail`() {
        val finding = Finding(
            target = "com.rezacah.ngaburake.Foo",
            type = FindingType.CLASS_NAME,
            severity = Severity.WARNING,
            detail = """contains "quotes"""",
        )

        val output = ReportGenerator.generate(listOf(finding), ReportFormat.JSON)

        assertTrue(output.contains("""contains \"quotes\""""))
    }

    @Test(expected = IllegalStateException::class)
    fun `html format belum diimplementasi, throw error jelas`() {
        ReportGenerator.generate(findings, ReportFormat.HTML)
    }
}
