package com.jalo.ngaburake.plugin

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
                id("com.jalo.ngaburake.obfuscation-verify")
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
    fun `class ter-obfuscate menghasilkan build SUCCESS dan report ditulis ke disk`() {
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
    fun `class tidak ter-obfuscate dengan failOnViolation true membuat build FAILED`() {
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
    fun `class tidak ter-obfuscate dengan failOnViolation default false tetap build SUCCESS tapi report tandai FAIL`() {
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
}
