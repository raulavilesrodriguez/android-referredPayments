package com.avilesrodriguez.data.datasource.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")
class AuthPreferencesDataSource @Inject constructor(
    private val context: Context
) {
    companion object{
        private val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
    }

    suspend fun isFirstTime(): Boolean =
        context.dataStore.data.map { preferences ->
            preferences[IS_FIRST_TIME] ?: true
        }.first()

    suspend fun setNotFirstTime() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_TIME] = false
        }
    }
}