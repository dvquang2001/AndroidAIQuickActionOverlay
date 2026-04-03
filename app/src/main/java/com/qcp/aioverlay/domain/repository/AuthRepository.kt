package com.qcp.aioverlay.domain.repository

import com.qcp.aioverlay.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun currentUser(): Flow<AuthUser?>
    suspend fun signIn(email: String, password: String): AuthUser
    suspend fun signUp(email: String, password: String): AuthUser
    suspend fun signOut()
}
