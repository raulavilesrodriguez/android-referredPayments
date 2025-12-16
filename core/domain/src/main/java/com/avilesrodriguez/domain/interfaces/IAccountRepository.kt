package com.avilesrodriguez.domain.interfaces

interface IAccountRepository {
    val currentUserId: String
    val hasUser: Boolean
    suspend fun signUp(email: String, password: String)
    suspend fun signIn(email: String, password: String)
    suspend fun sendRecoveryEmail(email: String)
    suspend fun signOut()
    suspend fun deleteAccount()
}