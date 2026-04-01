package com.qcp.aioverlay.overlay

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qcp.aioverlay.domain.usecase.ProcessTextUseCase
import com.qcp.aioverlay.ui.overlay.OverlayViewModel

fun overlayViewModelFactory(
    processTextUseCase: ProcessTextUseCase,
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        OverlayViewModel(processTextUseCase)
    }
}