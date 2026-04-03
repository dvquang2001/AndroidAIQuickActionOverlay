package com.qcp.aioverlay.data.repository

import com.qcp.aioverlay.data.auth.FirebaseAuthSource
import com.qcp.aioverlay.domain.model.AuthUser
import com.qcp.aioverlay.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val source: FirebaseAuthSource
) : AuthRepository {
    override fun currentUser(): Flow<AuthUser?> = source.currentUser()
    override suspend fun signIn(email: String, password: String): AuthUser = source.signIn(email, password)
    override suspend fun signUp(email: String, password: String): AuthUser = source.signUp(email, password)
    override suspend fun signOut() = source.signOut()
}
