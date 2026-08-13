package com.rezacah.ngaburake.runtime

import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.report.ReportFormat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class ObfuscationSDKTest {

    /** Fixture with only a `doWork()` method — used to verify custom keyword matching. */
    private class KeywordFixture {
        @Suppress("unused")
        fun doWork(): Boolean = true
    }

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

    @Test
    fun `addSensitivePackages adds every class in the list`() = runTest {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackages(
                listOf(ObfuscationSDKTest::class.java.name, KeywordFixture::class.java.name),
            )
            .build()

        val result = sdk.verify()

        // 2 classes * 2 checkers (ClassNameChecker + ReflectionChecker) = 4 findings.
        assertThat(result.findings).hasSize(4)
    }

    @Test
    fun `addSensitivePackages can be mixed with addSensitivePackage`() = runTest {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackages(listOf(ObfuscationSDKTest::class.java.name))
            .addSensitivePackage(KeywordFixture::class.java.name)
            .build()

        val result = sdk.verify()

        assertThat(result.findings).hasSize(4)
    }

    @Test
    fun `duplicate sensitive packages are deduplicated`() = runTest {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.example.PaymentManager")
            .addSensitivePackage("com.example.PaymentManager")
            .build()

        val result = sdk.verify()

        // 1 distinct class * 2 checkers = 2 findings, not 4.
        assertThat(result.findings).hasSize(2)
    }

    @Test
    fun `wildcard entry expands to every class under the package via the mapping file`() = runTest {
        val mappingFile = File.createTempFile("mapping", ".txt").apply {
            writeText(
                """
                com.example.payment.PaymentManager -> a:
                com.example.payment.Refund -> com.example.payment.Refund:
                com.example.other.Unrelated -> b:
                """.trimIndent(),
            )
        }
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.example.payment.*")
            .withMappingFile(mappingFile)
            .build()

        val result = sdk.verify()

        assertThat(result.findings.map { it.target }).containsAtLeast(
            "com.example.payment.PaymentManager",
            "com.example.payment.Refund",
        )
        assertThat(result.findings.none { it.target == "com.example.other.Unrelated" }).isTrue()
    }

    @Test
    fun `wildcard entry matching nothing produces a WARNING finding instead of being dropped`() = runTest {
        val mappingFile = File.createTempFile("mapping", ".txt").apply {
            writeText("com.example.other.Unrelated -> b:")
        }
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.example.doesnotexist.*")
            .withMappingFile(mappingFile)
            .build()

        val result = sdk.verify()

        val finding = result.findings.single()
        assertThat(finding.target).isEqualTo("com.example.doesnotexist.*")
        assertThat(finding.severity.toString()).isEqualTo("WARNING")
        assertThat(finding.detail).contains("wildcard matched no classes")
    }

    @Test
    fun `wildcard entry without withMappingFile throws IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> {
            ObfuscationSDK.Builder()
                .addSensitivePackage("com.example.payment.*")
                .build()
        }
        assertThat(exception.message).contains("withMappingFile")
    }

    @Test
    fun `withSensitiveKeywords overrides the ReflectionChecker keyword list`() = runTest {
        val defaultSdk = ObfuscationSDK.Builder()
            .addSensitivePackage(KeywordFixture::class.java.name)
            .build()
        val customSdk = ObfuscationSDK.Builder()
            .addSensitivePackage(KeywordFixture::class.java.name)
            .withSensitiveKeywords(listOf("doWork"))
            .build()

        val defaultResult = defaultSdk.verify()
        val customResult = customSdk.verify()

        assertThat(defaultResult.findings.none { it.detail.contains("doWork") }).isTrue()
        assertThat(customResult.findings.any { it.severity.toString() == "CRITICAL" }).isTrue()
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
