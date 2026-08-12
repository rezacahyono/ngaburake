package com.rezacah.ngaburake

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.data.ObfuscationRepositoryImpl
import com.rezacah.ngaburake.report.ReportFormat
import com.rezacah.ngaburake.report.Severity
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

/** Unit test for the repository layer's mapping-file wiring, using an injected provider. */
class ObfuscationRepositoryTest {

    @Test
    fun `verify with mapping file passes the provided mapping to the SDK`() = runTest {
        val mappingFile = File.createTempFile("mapping", ".txt").apply {
            writeText("com.example.PaymentManager -> a.b.c:")
        }
        val passedMapping = mutableListOf<File?>()
        val repo = ObfuscationRepositoryImpl(
            context = mockk(relaxed = true),
            mappingFileProvider = { mappingFile },
            sdkFactory = { _, mapping, _ ->
                passedMapping.add(mapping)
                com.rezacah.ngaburake.runtime.ObfuscationSDK.Builder()
                    .addSensitivePackage("com.example.PaymentManager")
                    .withMappingFile(mappingFile)
                    .build()
            },
        )

        val result = repo.verify(
            sensitivePackages = listOf("com.example.PaymentManager"),
            useMappingFile = true,
            sensitiveKeywords = listOf("apiKey"),
        )

        assertThat(passedMapping.single()).isEqualTo(mappingFile)
        // Class found in mapping and renamed -> the class-name checker reports OK.
        assertThat(result.findings.any { it.severity == Severity.OK }).isTrue()
    }

    @Test
    fun `verify without mapping file passes null mapping to the SDK`() = runTest {
        val passedMapping = mutableListOf<File?>()
        val repo = ObfuscationRepositoryImpl(
            context = mockk(relaxed = true),
            mappingFileProvider = { error("should not be called") },
            sdkFactory = { _, mapping, _ ->
                passedMapping.add(mapping)
                com.rezacah.ngaburake.runtime.ObfuscationSDK.Builder()
                    .addSensitivePackage("com.example.AnyClass")
                    .build()
            },
        )

        repo.verify(
            sensitivePackages = listOf("com.example.AnyClass"),
            useMappingFile = false,
            sensitiveKeywords = listOf("apiKey"),
        )

        assertThat(passedMapping.single()).isNull()
    }

    @Test
    fun `generateReport reuses the SDK built during verify and does not throw`() = runTest {
        val repo = ObfuscationRepositoryImpl(
            context = mockk(relaxed = true),
            sdkFactory = { packages, _, _ ->
                com.rezacah.ngaburake.runtime.ObfuscationSDK.Builder()
                    .addSensitivePackage(packages.first())
                    .build()
            },
        )

        val result = repo.verify(
            sensitivePackages = listOf("com.example.PaymentManager"),
            useMappingFile = false,
            sensitiveKeywords = listOf("apiKey"),
        )

        val console = repo.generateReport(result, ReportFormat.CONSOLE)
        val json = repo.generateReport(result, ReportFormat.JSON)

        assertThat(console).contains("com.example.PaymentManager")
        assertThat(json).contains("\"target\"")
    }

    @Test
    fun `generateReport before verify throws a clear error`() {
        val repo = ObfuscationRepositoryImpl(
            context = mockk(relaxed = true),
        )
        val result = com.rezacah.ngaburake.runtime.ObfuscationResult(
            isObfuscated = true,
            findings = emptyList(),
        )

        val exception = assertThrows<IllegalStateException> {
            repo.generateReport(result, ReportFormat.CONSOLE)
        }

        assertThat(exception.message).contains("verify() must run")
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
