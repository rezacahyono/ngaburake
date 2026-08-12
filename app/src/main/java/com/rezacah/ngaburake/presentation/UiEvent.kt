package com.rezacah.ngaburake.presentation

/** One-shot UI events (snackbar), kept separate from state per MVVM UDF. */
sealed interface UiEvent {
    data class ShowMessage(val message: String) : UiEvent
}
