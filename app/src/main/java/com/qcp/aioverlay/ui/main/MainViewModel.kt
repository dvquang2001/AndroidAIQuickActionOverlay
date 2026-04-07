package com.qcp.aioverlay.ui.main

import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.viewModelScope
import com.qcp.aioverlay.domain.model.HistoryItem
import com.qcp.aioverlay.domain.repository.HistoryRepository
import com.qcp.aioverlay.domain.usecase.CheckPermissionsUseCase
import com.qcp.aioverlay.domain.usecase.SignOutUseCase
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
    private val repository: HistoryRepository,
    private val signOutUseCase: SignOutUseCase,
    private val checkPermissionsUseCase: CheckPermissionsUseCase
) : BaseViewModel<MainUiState, MainIntent, MainEffect>(MainUiState()) {

    init {
        observeHistory()
        // Populate real permission state as soon as the ViewModel is created.
        // Uses onIntent so it runs inside viewModelScope (not the constructor thread).
        onIntent(MainIntent.RefreshPermission)
    }

    private fun observeHistory() {
        repository.observeHistory()
            .onEach { list -> updateState { copy(history = list) } }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: MainIntent) {
        when (intent) {

            // ── Permission navigation — only open settings when the permission is OFF ──
            MainIntent.OpenAccessibilitySettings -> {
                if (!state.value.isServiceEnabled) {
                    emitEffect(MainEffect.NavigateTo(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)))
                }
                // If already enabled, do nothing — the user tapped an active card by mistake.
            }

            MainIntent.OpenOverlaySettings -> {
                if (!state.value.hasOverlayPermission) {
                    emitEffect(
                        MainEffect.NavigateTo(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                            )
                        )
                    )
                }
            }

            // ── Re-read real system state (called on every onResume) ──────────────
            MainIntent.RefreshPermission -> {
                val result = checkPermissionsUseCase()
                updateState {
                    copy(
                        isServiceEnabled = result.isAccessibilityEnabled,
                        hasOverlayPermission = result.hasOverlayPermission
                    )
                }
            }

            // ── History ───────────────────────────────────────────────────────────
            is MainIntent.DeleteHistory -> repository.delete(id = intent.id)
            MainIntent.ClearHistory -> repository.clearAll()

            // ── Auth ──────────────────────────────────────────────────────────────
            MainIntent.SignOut -> {
                signOutUseCase()
                emitEffect(MainEffect.NavigateToLogin)
            }
        }
    }
}

data class MainUiState(
    val isServiceEnabled: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val history: List<HistoryItem> = emptyList()
) : UiState

sealed interface MainIntent : UiIntent {
    data object OpenAccessibilitySettings : MainIntent
    data object OpenOverlaySettings : MainIntent
    data class DeleteHistory(val id: Long) : MainIntent
    data object ClearHistory : MainIntent
    data object RefreshPermission : MainIntent
    data object SignOut : MainIntent
}

sealed interface MainEffect : UIEffect {
    data class NavigateTo(val intent: Intent) : MainEffect
    data object NavigateToLogin : MainEffect
}
