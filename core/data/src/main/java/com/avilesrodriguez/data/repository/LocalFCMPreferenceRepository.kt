package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.dataStore.LocalFCMPreferenceDataSource
import com.avilesrodriguez.domain.interfaces.ILocalFCMPreference
import javax.inject.Inject

class LocalFCMPreferenceRepository @Inject constructor(
    private val data: LocalFCMPreferenceDataSource
) : ILocalFCMPreference {
    override suspend fun getLastFCMToken(): Pair<String, String> {
        return data.getLastFCMToken()
    }

    override suspend fun saveLastRegistration(uid:String, token: String) {
        data.saveLastRegistration(uid, token)
    }

    override suspend fun clearLocalCache() = data.clearLocalCache()
}