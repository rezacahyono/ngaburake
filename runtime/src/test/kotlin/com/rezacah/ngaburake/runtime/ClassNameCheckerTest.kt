package com.rezacah.ngaburake.runtime

import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.mapping.MappingEntry
import com.rezacah.ngaburake.mapping.MappingIndex
import com.rezacah.ngaburake.report.Severity
import org.junit.Test

/** A fixture class with a deliberately short, non-descriptive name to simulate R8 renaming. */
private class a

class ClassNameCheckerTest {

    private val fqcnOfShortClass = "com.rezacah.ngaburake.runtime.a"
    private val fqcnOfThisTest = ClassNameCheckerTest::class.java.name

    @Test
    fun `without a mapping index, a class with a short simple name is considered obfuscated`() {
        val checker = ClassNameChecker(mappingIndex = null)

        val finding = checker.check(fqcnOfShortClass)

        assertThat(finding.severity).isEqualTo(Severity.OK)
    }

    @Test
    fun `without a mapping index, a class with a descriptive simple name is considered a warning`() {
        val checker = ClassNameChecker(mappingIndex = null)

        val finding = checker.check(fqcnOfThisTest)

        assertThat(finding.severity).isEqualTo(Severity.WARNING)
    }

    @Test
    fun `without a mapping index, a class not found via reflection is considered obfuscated`() {
        val checker = ClassNameChecker(mappingIndex = null)

        val finding = checker.check("com.rezacah.ngaburake.runtime.DoesNotExist")

        assertThat(finding.severity).isEqualTo(Severity.OK)
    }

    @Test
    fun `with a mapping index, an Obfuscated result from the mapping file takes priority`() {
        val index = MappingIndex(
            listOf(MappingEntry(original = fqcnOfThisTest, obfuscated = "a.b.c")),
        )
        val checker = ClassNameChecker(mappingIndex = index)

        val finding = checker.check(fqcnOfThisTest)

        assertThat(finding.severity).isEqualTo(Severity.OK)
        assertThat(finding.detail).contains("mapping file")
    }

    @Test
    fun `with a mapping index, a NotObfuscated result from the mapping file becomes CRITICAL`() {
        val index = MappingIndex(
            listOf(MappingEntry(original = fqcnOfThisTest, obfuscated = fqcnOfThisTest)),
        )
        val checker = ClassNameChecker(mappingIndex = index)

        val finding = checker.check(fqcnOfThisTest)

        assertThat(finding.severity).isEqualTo(Severity.CRITICAL)
    }

    @Test
    fun `with a mapping index but class absent from it, falls back to the reflection heuristic`() {
        val index = MappingIndex(emptyList())
        val checker = ClassNameChecker(mappingIndex = index)

        val finding = checker.check(fqcnOfShortClass)

        assertThat(finding.severity).isEqualTo(Severity.OK)
    }
}
