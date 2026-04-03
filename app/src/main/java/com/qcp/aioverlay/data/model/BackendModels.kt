package com.qcp.aioverlay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BackendAiRequest(
    val userId: String,
    val action: String,
    val text: String,
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "en"
)

@Serializable
data class BackendAiResponse(
    val success: Boolean,
    val requestId: String,
    val userId: String,
    val action: String,
    val result: String,
    val cached: Boolean = false,
    val processingTimeMs: Long,
)

@Serializable
data class BackendErrorResponse(
    val success: Boolean = false,
    val code: String,
    val message: String
)
