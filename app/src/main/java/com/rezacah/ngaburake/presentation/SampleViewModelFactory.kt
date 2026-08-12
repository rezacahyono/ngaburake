package com.rezacah.ngaburake.presentation

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rezacah.ngaburake.data.ObfuscationRepository
import com.rezacah.ngaburake.data.ObfuscationRepositoryImpl

/**
 * Creates the [ObfuscationCheckViewModel] with its [SavedStateHandle] and a real
 * [ObfuscationRepository]. Manual DI (no Hilt) — consistent with the SDK's consumer constraints.
 */
object SampleViewModelFactory {

    fun create(application: Application): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val repository: ObfuscationRepository = ObfuscationRepositoryImpl(application)
            ObfuscationCheckViewModel(createSavedStateHandle(), repository)
        }
    }
}
