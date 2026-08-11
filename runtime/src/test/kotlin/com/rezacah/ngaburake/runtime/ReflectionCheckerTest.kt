package com.rezacah.ngaburake.runtime

import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.report.Severity
import org.junit.Test

/** Fixtures with sensitive-looking and clean member names for [ReflectionChecker] to inspect. */
private class LeakyFixture {
    @Suppress("unused")
    val apiKey: String = "dummy"
}

private class CleanFixture {
    @Suppress("unused")
    fun doWork(): Boolean = true
}

class ReflectionCheckerTest {

    @Test
    fun `class with a field containing a sensitive keyword is detected as CRITICAL`() {
        val checker = ReflectionChecker()

        val finding = checker.check(LeakyFixture::class.java.name)

        assertThat(finding.severity).isEqualTo(Severity.CRITICAL)
        assertThat(finding.detail).contains("apiKey")
    }

    @Test
    fun `class without any sensitive member is detected as OK`() {
        val checker = ReflectionChecker()

        val finding = checker.check(CleanFixture::class.java.name)

        assertThat(finding.severity).isEqualTo(Severity.OK)
    }

    @Test
    fun `class not found via reflection is considered WARNING`() {
        val checker = ReflectionChecker()

        val finding = checker.check("com.rezacah.ngaburake.runtime.DoesNotExist")

        assertThat(finding.severity).isEqualTo(Severity.WARNING)
    }

    @Test
    fun `custom keyword list is used in place of the default`() {
        val checker = ReflectionChecker(sensitiveKeywords = listOf("doWork"))

        val finding = checker.check(CleanFixture::class.java.name)

        assertThat(finding.severity).isEqualTo(Severity.CRITICAL)
    }
}
