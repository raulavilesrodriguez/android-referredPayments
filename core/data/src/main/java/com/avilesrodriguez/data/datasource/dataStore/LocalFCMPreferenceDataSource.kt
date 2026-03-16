package com.avilesrodriguez.data.datasource.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fcmDataStore by preferencesDataStore(name = "token_fcm")
@Singleton
class LocalFCMPreferenceDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.fcmDataStore

    companion object {
        private val LAST_FCM_TOKEN = stringPreferencesKey("last_fcm_token")
        private val LAST_UID = stringPreferencesKey("last_uid")
    }

    suspend fun getLastFCMToken(): Pair<String, String> {
        val prefs = dataStore.data.first()
        return (prefs[LAST_UID] ?: "") to (prefs[LAST_FCM_TOKEN] ?: "")
    }

    suspend fun saveLastRegistration(uid: String, token: String) {
        dataStore.edit { preferences ->
            preferences[LAST_UID] = uid
            preferences[LAST_FCM_TOKEN] = token
        }
    }

    suspend fun clearLocalCache() {
        dataStore.edit { it.clear() }
    }
}
