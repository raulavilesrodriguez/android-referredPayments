package com.avilesrodriguez.domain.interfaces

interface IInternalTokenRepository {
    suspend fun storeFCMToken(uid: String, token: String)
    suspend fun clearFCMToken(uid: String)
}