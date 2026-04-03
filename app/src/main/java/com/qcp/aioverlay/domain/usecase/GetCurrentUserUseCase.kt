package com.qcp.aioverlay.domain.usecase

import com.qcp.aioverlay.domain.model.AuthUser
import com.qcp.aioverlay.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> = repository.currentUser()
}
