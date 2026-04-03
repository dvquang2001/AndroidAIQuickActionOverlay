package com.qcp.aioverlay.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.qcp.aioverlay.domain.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun currentUser(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): AuthUser =
        auth.signInWithEmailAndPassword(email, password).await()
            .user?.toDomain() ?: error("Sign in returned null user")

    suspend fun signUp(email: String, password: String): AuthUser =
        auth.createUserWithEmailAndPassword(email, password).await()
            .user?.toDomain() ?: error("Sign up returned null user")

    fun signOut() = auth.signOut()

    private fun FirebaseUser.toDomain() = AuthUser(uid = uid, email = email)
}
