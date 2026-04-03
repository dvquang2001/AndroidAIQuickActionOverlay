package com.qcp.aioverlay.domain.model

sealed interface AuthResult {
    data object Loading : AuthResult
    data class Success(val user: AuthUser) : AuthResult
    data class Error(val message: String) : AuthResult
}
