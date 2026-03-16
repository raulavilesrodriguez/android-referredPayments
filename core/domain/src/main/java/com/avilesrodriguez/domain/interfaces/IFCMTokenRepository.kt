package com.avilesrodriguez.domain.interfaces

interface IFCMTokenRepository {
    suspend fun getFCMToken(): String
}