package com.qcp.aioverlay.domain.usecase

import com.qcp.aioverlay.data.ai.GeminiClient
import com.qcp.aioverlay.domain.model.ActionType
import com.qcp.aioverlay.domain.model.OverlayAction
import com.qcp.aioverlay.domain.model.ProcessResult
import com.qcp.aioverlay.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ProcessTextUseCase @Inject constructor(
    private val gemini: GeminiClient,
    private val repository: HistoryRepository
) {

    operator fun invoke(action: OverlayAction): Flow<ProcessResult> {
        val prompt = buildPrompt(action)
        val buffer = StringBuilder()

        return gemini.streamResponse(prompt)
            .map { chunk ->
                buffer.append(chunk)
                ProcessResult.Success(buffer.toString()) as ProcessResult
            }
            .onStart { emit(ProcessResult.Loading) }
            .catch { e ->
                emit(ProcessResult.Error(e.message ?: "Error"))
            }
    }

    suspend fun saveToHistory(action: OverlayAction, output: String) {
        repository.save(
            input = action.inputText,
            output = output,
            type = action.actionType
        )
    }

    private fun buildPrompt(action: OverlayAction): String {
        val basePrompt = if(action.actionType == ActionType.CUSTOM) {
            action.customPrompt ?: ""
        } else {
            action.actionType.prompt
        }
        return "$basePrompt${action.inputText}"
    }
}