package com.rezacah.ngaburake.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rezacah.ngaburake.data.ObfuscationRepository
import com.rezacah.ngaburake.report.ReportFormat
import com.rezacah.ngaburake.report.Severity
import com.rezacah.ngaburake.runtime.ObfuscationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * MVVM ViewModel for the sample screen: keeps config state in [SavedStateHandle] (survives
 * process death / configuration change), delegates verification to an [ObfuscationRepository],
 * and exposes a single [uiState] [StateFlow] plus one-shot [events].
 */
class ObfuscationCheckViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ObfuscationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val sensitivePackages: StateFlow<List<String>> =
        savedStateHandle.getStateFlow(KEY_PACKAGES, DEFAULT_SENSITIVE_PACKAGES.toMutableList())

    val useMappingFile: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_MAPPING, true)

    val customKeywords: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_KEYWORDS, false)

    fun addSensitivePackage(className: String) {
        val trimmed = className.trim()
        if (trimmed.isEmpty()) return
        savedStateHandle[KEY_PACKAGES] =
            (sensitivePackages.value + trimmed).distinct().toMutableList()
    }

    fun removeSensitivePackage(className: String) {
        savedStateHandle[KEY_PACKAGES] = (sensitivePackages.value - className).toMutableList()
    }

    fun setUseMappingFile(enabled: Boolean) {
        savedStateHandle[KEY_MAPPING] = enabled
    }

    fun setCustomKeywords(enabled: Boolean) {
        savedStateHandle[KEY_KEYWORDS] = enabled
    }

    fun verify() {
        val packages = sensitivePackages.value
        if (packages.isEmpty()) {
            viewModelScope.launch {
                _events.send(UiEvent.ShowMessage("Add at least one sensitive class first."))
            }
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                repository.verify(
                    sensitivePackages = packages,
                    useMappingFile = useMappingFile.value,
                    sensitiveKeywords = if (customKeywords.value) CUSTOM_KEYWORDS else DEFAULT_KEYWORDS,
                )
            }.fold(
                onSuccess = { result ->
                    _uiState.value = UiState.Success(result)
                    if (result.findings.any { it.severity == Severity.CRITICAL }) {
                        _events.send(
                            UiEvent.ShowMessage(
                                "CRITICAL: ${result.findings.count { it.severity == Severity.CRITICAL }} violation(s) found",
                            ),
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.value = UiState.Error(e.message ?: e.javaClass.simpleName)
                },
            )
        }
    }

    /** Renders [result] via the repository, showing off [ObfuscationRepository.generateReport]. */
    fun generateReportText(result: ObfuscationResult, format: ReportFormat): String =
        repository.generateReport(result, format)

    companion object {
        private const val KEY_PACKAGES = "sensitive_packages"
        private const val KEY_MAPPING = "use_mapping_file"
        private const val KEY_KEYWORDS = "custom_keywords"

        val DEFAULT_SENSITIVE_PACKAGES = listOf(
            "com.rezacah.ngaburake.data.fixture.PaymentManager",
            "com.rezacah.ngaburake.data.fixture.ApiKeyStore",
            "com.rezacah.ngaburake.data.fixture.TokenStore",
            "com.rezacah.ngaburake.data.fixture.LegacyAuthManager",
            "com.rezacah.ngaburake.DoesNotExist", // demo: NotFoundInMapping -> WARNING
        )
        val DEFAULT_KEYWORDS = listOf("apiKey", "secret", "token", "password")
        val CUSTOM_KEYWORDS = listOf("legacy")
    }
}
