package com.rezacah.ngaburake.runtime

import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.report.ReportFormat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class ObfuscationSDKTest {

    @Test
    fun `build without a sensitive package throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            ObfuscationSDK.Builder().build()
        }
        assertThat(exception.message).contains("addSensitivePackage")
    }

    @Test
    fun `build with at least one sensitive package succeeds`() {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.example.PaymentManager")
            .build()

        assertThat(sdk).isNotNull()
    }

    @Test
    fun `verify without a mapping file runs the reflection heuristic`() = runTest {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage(ObfuscationSDKTest::class.java.name)
            .build()

        val result = sdk.verify()

        assertThat(result.findings).hasSize(2) // ClassNameChecker + ReflectionChecker
    }

    @Test
    fun `verify with a mapping file performs a cross-check`(): Unit = runTest {
        val mappingFile = File.createTempFile("mapping", ".txt").apply {
            writeText("com.example.PaymentManager -> a.b.c:")
        }
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.example.PaymentManager")
            .withMappingFile(mappingFile)
            .build()

        val result = sdk.verify()

        val classNameFinding = result.findings.first { it.target == "com.example.PaymentManager" }
        assertThat(classNameFinding.detail).contains("mapping file")
    }

    @Test
    fun `generateReport delegates to ReportGenerator without error for a supported format`() {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.example.Foo")
            .build()
        val result = ObfuscationResult(isObfuscated = true, findings = emptyList())

        val report = sdk.generateReport(result, ReportFormat.CONSOLE)

        assertThat(report).isNotEmpty()
    }

    private inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
        try {
            block()
        } catch (e: Throwable) {
            if (e is T) return e
            throw e
        }
        throw AssertionError("Expected ${T::class.java.name} to be thrown, but nothing was thrown.")
    }
}
