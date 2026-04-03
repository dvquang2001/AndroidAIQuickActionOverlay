package com.qcp.aioverlay.ui.auth

import androidx.lifecycle.viewModelScope
import com.qcp.aioverlay.domain.model.AuthResult
import com.qcp.aioverlay.domain.usecase.GetCurrentUserUseCase
import com.qcp.aioverlay.domain.usecase.SignInUseCase
import com.qcp.aioverlay.ui.base.BaseViewModel
import com.qcp.aioverlay.ui.base.UIEffect
import com.qcp.aioverlay.ui.base.UiIntent
import com.qcp.aioverlay.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : BaseViewModel<LoginUiState, LoginIntent, LoginEffect>(LoginUiState()) {

    init {
        checkAlreadyLoggedIn()
    }

    private fun checkAlreadyLoggedIn() {
        viewModelScope.launch {
            if (getCurrentUserUseCase().first() != null) {
                emitEffect(LoginEffect.NavigateToMain)
            }
        }
    }

    override suspend fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> updateState { copy(email = intent.value, error = null) }
            is LoginIntent.PasswordChanged -> updateState { copy(password = intent.value, error = null) }
            LoginIntent.Submit -> signIn()
            LoginIntent.NavigateToRegister -> emitEffect(LoginEffect.NavigateToRegister)
        }
    }

    private fun signIn() {
        val s = state.value
        if (s.isLoading) return
        signInUseCase(s.email.trim(), s.password)
            .onEach { result ->
                when (result) {
                    AuthResult.Loading -> updateState { copy(isLoading = true, error = null) }
                    is AuthResult.Success -> {
                        updateState { copy(isLoading = false) }
                        emitEffect(LoginEffect.NavigateToMain)
                    }
                    is AuthResult.Error -> updateState { copy(isLoading = false, error = result.message) }
                }
            }
            .launchIn(viewModelScope)
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed interface LoginIntent : UiIntent {
    data class EmailChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data object Submit : LoginIntent
    data object NavigateToRegister : LoginIntent
}

sealed interface LoginEffect : UIEffect {
    data object NavigateToMain : LoginEffect
    data object NavigateToRegister : LoginEffect
}
