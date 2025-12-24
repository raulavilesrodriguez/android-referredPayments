package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.dataStore.AuthPreferencesDataSource
import com.avilesrodriguez.domain.interfaces.IAuthPreferences
import javax.inject.Inject

class AuthPreferencesRepository @Inject constructor(
    private val data: AuthPreferencesDataSource
): IAuthPreferences {
    override suspend fun isFirstTime(): Boolean {
        return data.isFirstTime()
    }

    override suspend fun setNotFirstTime() {
        data.setNotFirstTime()
    }
}