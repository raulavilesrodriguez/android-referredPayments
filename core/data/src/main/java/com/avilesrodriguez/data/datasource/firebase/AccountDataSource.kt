package com.avilesrodriguez.data.datasource.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    val hasUser: Boolean
        get() = firebaseAuth.currentUser != null

    // sign up, registrar un nuevo usuario
    suspend fun signUp(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    // authentication with email and password. Iniciar sesión
    suspend fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun sendRecoveryEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun deleteAccount() {
        firebaseAuth.currentUser?.delete()?.await()
    }
}