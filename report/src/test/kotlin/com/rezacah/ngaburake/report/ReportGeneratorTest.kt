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
    fun `console format contains one pass-fail line per finding`() {
        val output = ReportGenerator.generate(findings, ReportFormat.CONSOLE)

        assertTrue(output.contains("[FAIL] com.rezacah.ngaburake.PaymentManager"))
        assertTrue(output.contains("[PASS] com.rezacah.ngaburake.ApiKeyStore"))
    }

    @Test
    fun `console format for empty list shows an info message`() {
        val output = ReportGenerator.generate(emptyList(), ReportFormat.CONSOLE)

        assertEquals("No sensitive classes configured.", output)
    }

    @Test
    fun `json format contains the minimal target type severity detail schema`() {
        val output = ReportGenerator.generate(findings, ReportFormat.JSON)

        assertTrue(output.contains("\"target\": \"com.rezacah.ngaburake.PaymentManager\""))
        assertTrue(output.contains("\"type\": \"CLASS_NAME\""))
        assertTrue(output.contains("\"severity\": \"CRITICAL\""))
        assertTrue(output.contains("\"detail\": \"not renamed\""))
    }

    @Test
    fun `json format escapes quotes in detail`() {
        val finding = Finding(
            target = "com.rezacah.ngaburake.Foo",
            type = FindingType.CLASS_NAME,
            severity = Severity.WARNING,
            detail = """contains "quotes"""",
        )

        val output = ReportGenerator.generate(listOf(finding), ReportFormat.JSON)

        assertTrue(output.contains("""contains \"quotes\""""))
    }

    @Test
    fun `html format produces a table with all findings`() {
        val output = ReportGenerator.generate(findings, ReportFormat.HTML)

        assertTrue(output.contains("<table>"))
        assertTrue(output.contains("com.rezacah.ngaburake.PaymentManager"))
        assertTrue(output.contains("com.rezacah.ngaburake.ApiKeyStore"))
        assertTrue(output.contains("class=\"critical\""))
        assertTrue(output.contains("class=\"ok\""))
    }

    @Test
    fun `html format escapes special html characters`() {
        val finding = Finding(
            target = "com.rezacah.ngaburake.Foo",
            type = FindingType.CLASS_NAME,
            severity = Severity.WARNING,
            detail = "contains <script>alert(1)</script>",
        )

        val output = ReportGenerator.generate(listOf(finding), ReportFormat.HTML)

        assertTrue(output.contains("&lt;script&gt;"))
        assertTrue(!output.contains("<script>alert"))
    }
}
