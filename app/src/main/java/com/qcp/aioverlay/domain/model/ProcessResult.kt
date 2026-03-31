package com.qcp.aioverlay.domain.model

sealed interface ProcessResult {
    data class Success(val output: String): ProcessResult
    data class Error(val message: String): ProcessResult
    data object Loading: ProcessResult
}