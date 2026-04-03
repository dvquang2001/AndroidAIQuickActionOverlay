package com.qcp.aioverlay.ui.auth

import androidx.lifecycle.viewModelScope
import com.qcp.aioverlay.domain.model.AuthResult
import com.qcp.aioverlay.domain.usecase.SignUpUseCase
import com.qcp.aioverlay.ui.base.BaseViewModel
import com.qcp.aioverlay.ui.base.UIEffect
import com.qcp.aioverlay.ui.base.UiIntent
import com.qcp.aioverlay.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : BaseViewModel<RegisterUiState, RegisterIntent, RegisterEffect>(RegisterUiState()) {

    override suspend fun handleIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.EmailChanged -> updateState { copy(email = intent.value, error = null) }
            is RegisterIntent.PasswordChanged -> updateState { copy(password = intent.value, error = null) }
            is RegisterIntent.ConfirmPasswordChanged -> updateState { copy(confirmPassword = intent.value, error = null) }
            RegisterIntent.Submit -> signUp()
            RegisterIntent.NavigateToLogin -> emitEffect(RegisterEffect.NavigateToLogin)
        }
    }

    private fun signUp() {
        val s = state.value
        if (s.isLoading) return
        if (s.password != s.confirmPassword) {
            updateState { copy(error = "Passwords do not match") }
            return
        }
        signUpUseCase(s.email.trim(), s.password)
            .onEach { result ->
                when (result) {
                    AuthResult.Loading -> updateState { copy(isLoading = true, error = null) }
                    is AuthResult.Success -> {
                        updateState { copy(isLoading = false) }
                        emitEffect(RegisterEffect.NavigateToMain)
                    }
                    is AuthResult.Error -> updateState { copy(isLoading = false, error = result.message) }
                }
            }
            .launchIn(viewModelScope)
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface RegisterIntent : UiIntent {
    data class EmailChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class ConfirmPasswordChanged(val value: String) : RegisterIntent
    data object Submit : RegisterIntent
    data object NavigateToLogin : RegisterIntent
}

sealed interface RegisterEffect : UIEffect {
    data object NavigateToMain : RegisterEffect
    data object NavigateToLogin : RegisterEffect
}
