package com.rezacah.ngaburake.runtime

import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.Severity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RuntimeCheckUseCaseTest {

    private fun fakeFinding(target: String, severity: Severity) =
        Finding(target = target, type = FindingType.CLASS_NAME, severity = severity, detail = "fake")

    @Test
    fun `execute runs every checker for every class and merges the results`() = runTest {
        val checkerA = mockk<ObfuscationChecker> {
            every { check(any()) } answers { fakeFinding(firstArg(), Severity.OK) }
        }
        val checkerB = mockk<ObfuscationChecker> {
            every { check(any()) } answers { fakeFinding(firstArg(), Severity.WARNING) }
        }
        val useCase = RuntimeCheckUseCase(
            checkers = listOf(checkerA, checkerB),
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val findings = useCase.execute(listOf("com.example.A", "com.example.B"))

        assertThat(findings).hasSize(4)
        assertThat(findings.count { it.severity == Severity.OK }).isEqualTo(2)
        assertThat(findings.count { it.severity == Severity.WARNING }).isEqualTo(2)
    }

    @Test
    fun `execute with an empty class list produces an empty finding list`() = runTest {
        val checker = mockk<ObfuscationChecker>()
        val useCase = RuntimeCheckUseCase(
            checkers = listOf(checker),
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val findings = useCase.execute(emptyList())

        assertThat(findings).isEmpty()
    }

    @Test
    fun `execute with an empty checker list produces an empty finding list`() = runTest {
        val useCase = RuntimeCheckUseCase(
            checkers = emptyList(),
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val findings = useCase.execute(listOf("com.example.A"))

        assertThat(findings).isEmpty()
    }
}
