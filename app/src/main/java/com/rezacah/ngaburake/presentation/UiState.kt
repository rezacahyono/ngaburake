package com.rezacah.ngaburake.presentation

import com.rezacah.ngaburake.runtime.ObfuscationResult

/** UI state for the sample screen, per MVVM UDF. */
sealed interface UiState {
    /** Initial state — nothing has been verified yet. */
    data object Idle : UiState
    data object Loading : UiState
    data class Success(val result: ObfuscationResult) : UiState
    data class Error(val message: String) : UiState
}
