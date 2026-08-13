package com.rezacah.ngaburake.plugin

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * End-to-end functional tests using Gradle TestKit — unlike [ObfuscationPluginTest] (which only
 * checks registration via `ProjectBuilder`), these actually run the `verifyObfuscation` task's
 * `@TaskAction` against a real fixture project on disk.
 */
class ObfuscationPluginFunctionalTest {

    @get:Rule
    val projectDir = TemporaryFolder()

    private fun writeSettings() {
        File(projectDir.root, "settings.gradle.kts").writeText(
            """rootProject.name = "fixture"""",
        )
    }

    private fun writeBuildScript(extensionBlock: String) {
        File(projectDir.root, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.rezacah.ngaburake.obfuscation-verify")
            }

            $extensionBlock
            """.trimIndent(),
        )
    }

    private fun writeMappingFile(content: String) {
        val mappingDir = File(projectDir.root, "build/outputs/mapping/release")
        mappingDir.mkdirs()
        File(mappingDir, "mapping.txt").writeText(content)
    }

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir.root)
            .withPluginClasspath()
            .withArguments(*args, "--stacktrace")

    @Test
    fun `obfuscated class produces a SUCCESS build and writes a report to disk`() {
        writeSettings()
        writeBuildScript(
            """
            obfuscationVerify {
                sensitivePackages.set(listOf("com.example.PaymentManager"))
            }
            """.trimIndent(),
        )
        writeMappingFile("com.example.PaymentManager -> a.b.c:")

        val result = runner("verifyObfuscation").build()

        assertThat(result.task(":verifyObfuscation")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val reportFile = File(projectDir.root, "build/reports/obfuscation/report.console")
        assertThat(reportFile.exists()).isTrue()
        assertThat(reportFile.readText()).contains("[PASS] com.example.PaymentManager")
    }

    @Test
    fun `non-obfuscated class with failOnViolation true makes the build FAILED`() {
        writeSettings()
        writeBuildScript(
            """
            obfuscationVerify {
                sensitivePackages.set(listOf("com.example.PaymentManager"))
                failOnViolation.set(true)
            }
            """.trimIndent(),
        )
        writeMappingFile("com.example.PaymentManager -> com.example.PaymentManager:")

        val result = runner("verifyObfuscation").buildAndFail()

        assertThat(result.task(":verifyObfuscation")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("com.example.PaymentManager")
    }

    @Test
    fun `non-obfuscated class with default failOnViolation false still builds SUCCESS but report marks FAIL`() {
        writeSettings()
        writeBuildScript(
            """
            obfuscationVerify {
                sensitivePackages.set(listOf("com.example.PaymentManager"))
            }
            """.trimIndent(),
        )
        writeMappingFile("com.example.PaymentManager -> com.example.PaymentManager:")

        val result = runner("verifyObfuscation").build()

        assertThat(result.task(":verifyObfuscation")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("[FAIL] com.example.PaymentManager")
    }

    @Test
    fun `wildcard entry expands to every class under the package in mapping txt`() {
        writeSettings()
        writeBuildScript(
            """
            obfuscationVerify {
                sensitivePackages.set(listOf("com.example.payment.*"))
            }
            """.trimIndent(),
        )
        writeMappingFile(
            """
            com.example.payment.PaymentManager -> a:
            com.example.payment.Refund -> com.example.payment.Refund:
            com.example.other.Unrelated -> b:
            """.trimIndent(),
        )

        val result = runner("verifyObfuscation").build()

        assertThat(result.task(":verifyObfuscation")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("[PASS] com.example.payment.PaymentManager")
        assertThat(result.output).contains("[FAIL] com.example.payment.Refund")
        assertThat(result.output).doesNotContain("com.example.other.Unrelated")
    }

    @Test
    fun `wildcard entry matching nothing produces a WARN finding instead of silently doing nothing`() {
        writeSettings()
        writeBuildScript(
            """
            obfuscationVerify {
                sensitivePackages.set(listOf("com.example.doesnotexist.*"))
            }
            """.trimIndent(),
        )
        writeMappingFile("com.example.other.Unrelated -> b:")

        val result = runner("verifyObfuscation").build()

        assertThat(result.task(":verifyObfuscation")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output).contains("[WARN] com.example.doesnotexist.*")
        assertThat(result.output).contains("wildcard matched no classes")
    }
}
