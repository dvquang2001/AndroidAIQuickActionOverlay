package com.qcp.aioverlay.ui.overlay

import androidx.lifecycle.viewModelScope
import com.qcp.aioverlay.domain.model.ActionType
import com.qcp.aioverlay.domain.model.OverlayAction
import com.qcp.aioverlay.domain.model.ProcessResult
import com.qcp.aioverlay.domain.usecase.ProcessTextUseCase
import com.qcp.aioverlay.ui.base.BaseViewModel
import com.qcp.aioverlay.ui.base.UIEffect
import com.qcp.aioverlay.ui.base.UiIntent
import com.qcp.aioverlay.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val processTextUseCase: ProcessTextUseCase
) : BaseViewModel<OverlayUiState, OverlayIntent, OverlayEffect>(OverlayUiState()){

    override suspend fun handleIntent(intent: OverlayIntent) {
        when(intent) {
            is OverlayIntent.SetText -> updateState { copy(selectedText = intent.text) }

            is OverlayIntent.SelectAction ->
                updateState { copy(selectedAction = intent.type, result = null) }

            is OverlayIntent.SetCustomPrompt ->
                updateState { copy(customPrompt = intent.prompt) }

            OverlayIntent.ToggleExpanded ->
                updateState { copy(isExpanded = !isExpanded) }

            OverlayIntent.RunAction -> runAction()

            OverlayIntent.CopyResult -> {
                val output = (state.value.result as? ProcessResult.Success)?.output ?: return
                emitEffect(OverlayEffect.CopyToClipboard(output))
            }

            OverlayIntent.Dismiss -> emitEffect(OverlayEffect.Dismiss)
        }
    }

    private fun runAction() {
        val s = state.value
        if(s.selectedText.isBlank()) return

        val action = OverlayAction(
            inputText = s.selectedText,
            actionType =  s.selectedAction,
            customPrompt = s.customPrompt.takeIf { s.selectedAction == ActionType.CUSTOM }
        )

        var lastOutput = ""
        processTextUseCase(action)
            .onEach { result ->
                updateState { copy(result = result) }
                if(result is ProcessResult.Success) lastOutput = result.output
            }
            .launchIn(viewModelScope)
            .also {
                it.invokeOnCompletion { error ->
                    if(error == null && lastOutput.isNotBlank()) {
                        viewModelScope.let { scope ->
                            MainScope().let {
                                scope.launch {
                                    processTextUseCase.saveToHistory(action, lastOutput)
                                }
                            }
                        }
                    }
                }
            }
    }
}

data class OverlayUiState(
    val selectedText: String = "",
    val selectedAction: ActionType = ActionType.TRANSLATE,
    val result: ProcessResult? = null,
    val customPrompt: String = "",
    val isExpanded: Boolean = false,
): UiState

sealed interface OverlayIntent: UiIntent {
    data class SetText(val text: String): OverlayIntent
    data class SelectAction(val type: ActionType): OverlayIntent
    data class SetCustomPrompt(val prompt: String): OverlayIntent
    data object RunAction: OverlayIntent
    data object CopyResult: OverlayIntent
    data object Dismiss: OverlayIntent
    data object ToggleExpanded: OverlayIntent
}

sealed interface OverlayEffect: UIEffect {
    data class CopyToClipboard(val text: String): OverlayEffect
    data object Dismiss: OverlayEffect
    data class ShowError(val message: String): OverlayEffect
}