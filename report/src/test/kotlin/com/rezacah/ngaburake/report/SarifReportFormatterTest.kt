package com.rezacah.ngaburake.report

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SarifReportFormatterTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `output is valid sarif json with schema and version`() {
        val output = ReportGenerator.generate(
            listOf(
                Finding(
                    target = "com.rezacah.ngaburake.PaymentManager",
                    type = FindingType.CLASS_NAME,
                    severity = Severity.OK,
                    detail = "renamed as expected",
                ),
            ),
            ReportFormat.SARIF,
        )

        // Raw string checks, not just decoded-object checks: kotlinx.serialization skips fields
        // that equal their default value unless encodeDefaults = true is set, so decoding a log
        // that's missing $schema/version entirely would still produce these same default values
        // and silently pass a decode-only assertion.
        assertTrue(output.contains("\"\$schema\""))
        assertTrue(output.contains("\"version\": \"2.1.0\""))
        assertTrue(output.contains("\"name\": \"ngaburake\""))

        val log = json.decodeFromString(SarifLog.serializer(), output)

        assertEquals("2.1.0", log.version)
        assertEquals(1, log.runs.size)
    }

    @Test
    fun `declares one rule per distinct finding type`() {
        val findings = listOf(
            Finding("a", FindingType.CLASS_NAME, Severity.OK, "d"),
            Finding("b", FindingType.FIELD_NAME, Severity.CRITICAL, "d"),
            Finding("c", FindingType.CLASS_NAME, Severity.WARNING, "d"),
        )

        val log = json.decodeFromString(
            SarifLog.serializer(),
            ReportGenerator.generate(findings, ReportFormat.SARIF),
        )

        val ruleIds = log.runs.single().tool.driver.rules.map { it.id }
        assertEquals(listOf("obfuscation-class-name", "obfuscation-field-name"), ruleIds)
    }

    @Test
    fun `maps severity to sarif level for every severity including OK`() {
        val findings = listOf(
            Finding("a", FindingType.CLASS_NAME, Severity.OK, "ok"),
            Finding("b", FindingType.CLASS_NAME, Severity.WARNING, "warn"),
            Finding("c", FindingType.CLASS_NAME, Severity.CRITICAL, "crit"),
        )

        val log = json.decodeFromString(
            SarifLog.serializer(),
            ReportGenerator.generate(findings, ReportFormat.SARIF),
        )

        val levels = log.runs.single().results.map { it.level }
        assertEquals(listOf("note", "warning", "error"), levels)
    }

    @Test
    fun `finding without filePath uses logicalLocation only`() {
        val finding = Finding(
            target = "com.rezacah.ngaburake.ApiKeyStore",
            type = FindingType.CLASS_NAME,
            severity = Severity.CRITICAL,
            detail = "kept its original name",
        )

        val log = json.decodeFromString(
            SarifLog.serializer(),
            ReportGenerator.generate(listOf(finding), ReportFormat.SARIF),
        )

        val location = log.runs.single().results.single().locations.single()
        assertNull(location.physicalLocation)
        assertEquals(
            "com.rezacah.ngaburake.ApiKeyStore",
            location.logicalLocations!!.single().fullyQualifiedName,
        )
    }

    @Test
    fun `finding with filePath uses physicalLocation with resolved line`() {
        val finding = Finding(
            target = "com.rezacah.ngaburake.ApiKeyStore",
            type = FindingType.CLASS_NAME,
            severity = Severity.CRITICAL,
            detail = "kept its original name",
            filePath = "src/main/kotlin/com/rezacah/ngaburake/ApiKeyStore.kt",
            line = 12,
        )

        val log = json.decodeFromString(
            SarifLog.serializer(),
            ReportGenerator.generate(listOf(finding), ReportFormat.SARIF),
        )

        val location = log.runs.single().results.single().locations.single()
        assertNull(location.logicalLocations)
        assertEquals(
            "src/main/kotlin/com/rezacah/ngaburake/ApiKeyStore.kt",
            location.physicalLocation!!.artifactLocation.uri,
        )
        assertEquals(12, location.physicalLocation.region.startLine)
    }

    @Test
    fun `finding with filePath but no line defaults startLine to 1`() {
        val finding = Finding(
            target = "com.rezacah.ngaburake.ApiKeyStore",
            type = FindingType.CLASS_NAME,
            severity = Severity.CRITICAL,
            detail = "kept its original name",
            filePath = "src/main/kotlin/com/rezacah/ngaburake/ApiKeyStore.kt",
        )

        val log = json.decodeFromString(
            SarifLog.serializer(),
            ReportGenerator.generate(listOf(finding), ReportFormat.SARIF),
        )

        val physicalLocation = log.runs.single().results.single().locations.single().physicalLocation
        assertEquals(1, physicalLocation!!.region.startLine)
    }

    @Test
    fun `message text comes from finding detail`() {
        val finding = Finding(
            target = "com.rezacah.ngaburake.Foo",
            type = FindingType.METHOD_NAME,
            severity = Severity.CRITICAL,
            detail = "member name 'getApiKey' still contains a sensitive keyword",
        )

        val log = json.decodeFromString(
            SarifLog.serializer(),
            ReportGenerator.generate(listOf(finding), ReportFormat.SARIF),
        )

        assertTrue(
            log.runs.single().results.single().message.text
                .contains("member name 'getApiKey' still contains a sensitive keyword"),
        )
    }
}
