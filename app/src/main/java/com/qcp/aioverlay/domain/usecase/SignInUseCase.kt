package com.qcp.aioverlay.domain.usecase

import com.qcp.aioverlay.domain.model.AuthResult
import com.qcp.aioverlay.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val user = repository.signIn(email, password)
            emit(AuthResult.Success(user))
        } catch (e: Exception) {
            emit(AuthResult.Error(e.message ?: "Sign in failed"))
        }
    }
}
