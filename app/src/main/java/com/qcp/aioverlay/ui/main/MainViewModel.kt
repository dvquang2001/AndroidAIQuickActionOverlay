package com.qcp.aioverlay.ui.main

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.viewModelScope
import com.qcp.aioverlay.domain.model.HistoryItem
import com.qcp.aioverlay.domain.repository.HistoryRepository
import com.qcp.aioverlay.ui.base.BaseViewModel
import com.qcp.aioverlay.ui.base.UIEffect
import com.qcp.aioverlay.ui.base.UiIntent
import com.qcp.aioverlay.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HistoryRepository
) : BaseViewModel<MainUiState, MainIntent, MainEffect>(MainUiState()) {

    init {
        observeHistory()
    }

    private fun observeHistory() {
        repository.observeHistory()
            .onEach { list -> updateState { copy(history = list) } }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: MainIntent) {
        when(intent) {
            MainIntent.OpenAccessibilitySettings ->
                emitEffect(MainEffect.NavigateTo(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))

            MainIntent.OpenOverlaySettings ->
                emitEffect(MainEffect.NavigateTo(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)))

            is MainIntent.DeleteHistory -> repository.delete(id = intent.id)

            MainIntent.ClearHistory -> repository.clearAll()

            MainIntent.RefreshPermission -> {
                //todo: update later
            }
        }
    }
}

data class MainUiState(
    val isServiceEnabled: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val history: List<HistoryItem> = emptyList()
): UiState

sealed interface MainIntent: UiIntent {
    data object OpenAccessibilitySettings: MainIntent
    data object OpenOverlaySettings: MainIntent
    data class DeleteHistory(val id: Long): MainIntent
    data object ClearHistory: MainIntent
    data object RefreshPermission: MainIntent
}

sealed interface MainEffect: UIEffect {
    data class NavigateTo(val intent: Intent): MainEffect
}