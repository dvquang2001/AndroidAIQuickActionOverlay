package com.qcp.aioverlay.data.ai

import com.qcp.aioverlay.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiClient @Inject constructor() {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash-latest" ,
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            maxOutputTokens = 1024
        }
    )

    /**
     * Stream response token by token – great for showing live typing effect in overlay.
     */
    fun streamResponse(prompt: String): Flow<String> =
        model.generateContentStream(prompt)
            .map { chunk -> chunk.text ?: "" }
            .catch { e -> throw GeminiException(e.message ?: "Unknown AI error") }

    /**
     * One-shot response – simpler use cases.
     */
    suspend fun generateResponse(prompt: String): String =
        model.generateContent(prompt).text
            ?: throw GeminiException("Empty response from Gemini")
}

class GeminiException(message: String) : Exception(message)