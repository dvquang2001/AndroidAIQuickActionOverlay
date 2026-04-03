package com.qcp.aioverlay.domain.usecase

import com.qcp.aioverlay.data.ai.GeminiClient
import com.qcp.aioverlay.data.ai.GeminiException
import com.qcp.aioverlay.data.model.BackendAiRequest
import com.qcp.aioverlay.data.remote.BackendClient
import com.qcp.aioverlay.data.remote.BackendResult
import com.qcp.aioverlay.domain.model.ActionType
import com.qcp.aioverlay.domain.model.OverlayAction
import com.qcp.aioverlay.domain.model.ProcessResult
import com.qcp.aioverlay.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ProcessTextUseCase @Inject constructor(
    private val backendClient: BackendClient,
    private val repository: HistoryRepository
) {

    operator fun invoke(action: OverlayAction): Flow<ProcessResult> = flow {
        emit(ProcessResult.Loading)

        val request = BackendAiRequest(
            userId = "user-3", //todo: later replace with real userId
            action = action.actionType.value,
            text = action.inputText,
            targetLanguage = "en"
        )

        when(val result = backendClient.processAi(request)) {
            is BackendResult.Success -> {
                val output = result.response.result
                emit(ProcessResult.Success(output))
                saveToHistory(action, output)
            }

            is BackendResult.Error -> {
                val userMessage = mapErrorCode(result.code, result.message)
                emit(ProcessResult.Error(userMessage))
            }
        }
    }.catch { e ->
        emit(ProcessResult.Error(e.message ?: "Unknown error"))
    }

    suspend fun saveToHistory(action: OverlayAction, output: String) {
        repository.save(
            input = action.inputText,
            output = output,
            type = action.actionType
        )
    }

    private fun mapErrorCode(code: String, message: String): String = when(code) {
        "UNAUTHORIZED"        -> "App not authorized. Contact support."
        "RATE_LIMIT_EXCEEDED" -> "Too many requests. Please slow down."
        "DAILY_QUOTA_EXCEEDED"-> "Daily limit reached. Try again tomorrow."
        "DUPLICATE_REQUEST"   -> "Request already in progress."
        "GEMINI_TIMEOUT"      -> "AI service timed out. Try again."
        "GEMINI_RATE_LIMITED" -> "AI service busy. Try again shortly."
        "GEMINI_UNAUTHORIZED" -> "AI service configuration error."
        "NETWORK_ERROR"       -> "Network error: $message"
        else                  -> message
    }

    private val ActionType.value: String
        get() = when(this) {
            ActionType.TRANSLATE -> "translate"
            ActionType.SUMMARIZE -> "summarize"
            ActionType.EXPLAIN -> "explain"
            ActionType.CUSTOM -> "explain"   // fallback
        }
}
