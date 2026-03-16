package com.avilesrodriguez.domain.interfaces

interface ILocalFCMPreference {
    suspend fun getLastFCMToken(): Pair<String,String>
    suspend fun saveLastRegistration(uid:String, token: String)
    suspend fun clearLocalCache()
}