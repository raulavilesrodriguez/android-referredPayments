package com.avilesrodriguez.domain.interfaces

interface IAuthPreferences {
    suspend fun isFirstTime(): Boolean
    suspend fun setNotFirstTime()
}