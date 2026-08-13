package com.rezacah.ngaburake.testing

import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.Severity
import org.junit.Test

class FakeObfuscationCheckerTest {

    @Test
    fun `default severity OK is used for a class that was not configured`() {
        val fake = FakeObfuscationChecker()

        val finding = fake.check("com.example.AnyClass")

        assertThat(finding.severity).isEqualTo(Severity.OK)
    }

    @Test
    fun `default type is CLASS_NAME`() {
        val fake = FakeObfuscationChecker()

        val finding = fake.check("com.example.AnyClass")

        assertThat(finding.type).isEqualTo(FindingType.CLASS_NAME)
    }

    @Test
    fun `custom type overrides the default CLASS_NAME`() {
        val fake = FakeObfuscationChecker(type = FindingType.METHOD_NAME)

        val finding = fake.check("com.example.ApiKeyStore")

        assertThat(finding.type).isEqualTo(FindingType.METHOD_NAME)
    }

    @Test
    fun `findingsByClassName overrides the severity for a specific class`() {
        val fake = FakeObfuscationChecker(
            findingsByClassName = mapOf("com.example.PaymentManager" to Severity.CRITICAL),
        )

        val finding = fake.check("com.example.PaymentManager")

        assertThat(finding.severity).isEqualTo(Severity.CRITICAL)
    }

    @Test
    fun `class not covered by findingsByClassName still uses the custom defaultSeverity`() {
        val fake = FakeObfuscationChecker(
            findingsByClassName = mapOf("com.example.PaymentManager" to Severity.CRITICAL),
            defaultSeverity = Severity.WARNING,
        )

        val finding = fake.check("com.example.Unrelated")

        assertThat(finding.severity).isEqualTo(Severity.WARNING)
    }

    @Test
    fun `result is deterministic - repeated calls produce the same finding`() {
        val fake = FakeObfuscationChecker(
            findingsByClassName = mapOf("com.example.Foo" to Severity.WARNING),
        )

        val first = fake.check("com.example.Foo")
        val second = fake.check("com.example.Foo")

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `can be used as a regular ObfuscationChecker via RuntimeCheckUseCase-style usage`() {
        // Simulates a consumer using the fake exactly like a real checker — same interface,
        // no special-casing needed.
        val checker: com.rezacah.ngaburake.runtime.ObfuscationChecker = FakeObfuscationChecker()

        val finding = checker.check("com.example.Anything")

        assertThat(finding.severity).isEqualTo(Severity.OK)
    }
}
