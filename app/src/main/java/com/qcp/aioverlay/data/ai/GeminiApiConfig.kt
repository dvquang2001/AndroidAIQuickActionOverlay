package com.qcp.aioverlay.data.ai

import com.qcp.aioverlay.BuildConfig

object GeminiApiConfig {
    const val baseUrl = "https://generativelanguage.googleapis.com"
    const val apiVersion = "v1beta"
    private val defaultPreferredModels = listOf(
        "gemini-2.5-flash",
        "gemini-2.0-flash",
        "gemini-2.5-flash-lite"
    )

    val preferredModels: List<String>
        get() {
            val configured = BuildConfig.BACKEND_API_KEY
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            return if (configured.isNotEmpty()) {
                configured
            } else {
                defaultPreferredModels
            }
        }

    fun streamEndpoint(model: String): String =
        "$baseUrl/$apiVersion/models/$model:streamGenerateContent?alt=sse"

    fun listModelsEndpoint(): String =
        "$baseUrl/$apiVersion/models"
}
