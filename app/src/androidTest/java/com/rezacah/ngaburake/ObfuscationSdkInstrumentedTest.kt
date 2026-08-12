package com.rezacah.ngaburake

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.report.Severity
import com.rezacah.ngaburake.runtime.ObfuscationSDK
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end runtime verification on a real device: exercises the actual ObfuscationSDK against
 * the sample app's own fixture classes.
 */
@RunWith(AndroidJUnit4::class)
class ObfuscationSdkInstrumentedTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun verify_detectsSensitiveMembersOnDevice() = runBlocking {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.rezacah.ngaburake.data.fixture.ApiKeyStore")
            .addSensitivePackage("com.rezacah.ngaburake.data.fixture.TokenStore")
            .build()

        val result = sdk.verify()

        assertThat(result.findings).isNotEmpty()
        // ApiKeyStore.getApiKey() / TokenStore.secretToken leak via reflection on a device that
        // ships an unminified debug build.
        assertThat(result.findings.any { it.severity == Severity.CRITICAL }).isTrue()
    }

    @Test
    fun generateReport_producesAllThreeFormats() {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.rezacah.ngaburake.data.fixture.ApiKeyStore")
            .build()
        val result = runBlocking { sdk.verify() }

        val console = sdk.generateReport(result, com.rezacah.ngaburake.report.ReportFormat.CONSOLE)
        val json = sdk.generateReport(result, com.rezacah.ngaburake.report.ReportFormat.JSON)
        val html = sdk.generateReport(result, com.rezacah.ngaburake.report.ReportFormat.HTML)

        // Debug build is unminified, so ApiKeyStore keeps its descriptive name — the class-name
        // checker reports WARNING, but the console format must still render the class name.
        assertThat(console).contains("com.rezacah.ngaburake.data.fixture.ApiKeyStore")
        assertThat(json).contains("\"target\"")
        assertThat(html).contains("<table>")
    }

    @Test
    fun applicationId_isCorrect() {
        assertThat(context.packageName).isEqualTo("com.rezacah.ngaburake")
    }
}
