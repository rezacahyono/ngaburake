package com.rezacah.ngaburake

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.rezacah.ngaburake.data.ObfuscationRepository
import com.rezacah.ngaburake.presentation.ObfuscationCheckViewModel
import com.rezacah.ngaburake.presentation.UiState
import com.rezacah.ngaburake.report.Finding
import com.rezacah.ngaburake.report.FindingType
import com.rezacah.ngaburake.report.Severity
import com.rezacah.ngaburake.runtime.ObfuscationResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObfuscationCheckViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeRepository(
        result: ObfuscationResult = ObfuscationResult(
            isObfuscated = true,
            findings = listOf(
                Finding("com.example.A", FindingType.CLASS_NAME, Severity.OK, "ok"),
            ),
        ),
    ): ObfuscationRepository = mockk {
        coEvery { verify(any(), any(), any()) } returns result
    }

    private fun newViewModel(
        repository: ObfuscationRepository = fakeRepository(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = ObfuscationCheckViewModel(savedStateHandle, repository)

    @Test
    fun `verify publishes Success state with the repository result`() = runTest(testDispatcher) {
        val findings = listOf(
            Finding("com.example.A", FindingType.CLASS_NAME, Severity.OK, "ok"),
            Finding("com.example.B", FindingType.CLASS_NAME, Severity.CRITICAL, "leak"),
        )
        val viewModel = newViewModel(
            repository = fakeRepository(
                ObfuscationResult(isObfuscated = false, findings = findings),
            ),
        )

        viewModel.verify()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        val success = state as UiState.Success
        assertThat(success.result.isObfuscated).isFalse()
        assertThat(success.result.findings).hasSize(2)
    }

    @Test
    fun `addSensitivePackage appends and deduplicates`() = runTest(testDispatcher) {
        val viewModel = newViewModel()

        viewModel.addSensitivePackage("com.example.C")
        viewModel.addSensitivePackage("com.example.C")
        viewModel.addSensitivePackage("  com.example.D  ")

        assertThat(viewModel.sensitivePackages.value)
            .containsExactlyElementsIn(
                ObfuscationCheckViewModel.DEFAULT_SENSITIVE_PACKAGES + listOf("com.example.C", "com.example.D"),
            )
            .inOrder()
    }

    @Test
    fun `removeSensitivePackage removes the class`() = runTest(testDispatcher) {
        val viewModel = newViewModel()
        val first = ObfuscationCheckViewModel.DEFAULT_SENSITIVE_PACKAGES.first()

        viewModel.removeSensitivePackage(first)

        assertThat(viewModel.sensitivePackages.value).doesNotContain(first)
    }

    @Test
    fun `config state is restored from SavedStateHandle`() = runTest(testDispatcher) {
        // Simulates process death: state persisted in the handle is picked up by a new ViewModel.
        val savedStateHandle = SavedStateHandle().apply {
            this["sensitive_packages"] = mutableListOf("com.example.Kept")
            this["use_mapping_file"] = false
        }
        val viewModel = newViewModel(savedStateHandle = savedStateHandle)

        assertThat(viewModel.sensitivePackages.value).containsExactly("com.example.Kept")
        assertThat(viewModel.useMappingFile.value).isFalse()
    }

    @Test
    fun `verify with empty package list keeps state unchanged instead of failing`() = runTest(testDispatcher) {
        val viewModel = newViewModel()
        ObfuscationCheckViewModel.DEFAULT_SENSITIVE_PACKAGES.forEach(viewModel::removeSensitivePackage)

        viewModel.verify()
        testDispatcher.scheduler.advanceUntilIdle()

        // No packages -> the guard returns early; state stays at its initial Idle value.
        assertThat(viewModel.uiState.value).isEqualTo(UiState.Idle)
    }
}
