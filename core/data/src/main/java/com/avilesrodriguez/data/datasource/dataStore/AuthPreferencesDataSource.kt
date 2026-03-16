package com.avilesrodriguez.data.datasource.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")
@Singleton
class AuthPreferencesDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.dataStore
    companion object{
        private val IS_FIRST_TIME = booleanPreferencesKey("is_first_time")
    }

    suspend fun isFirstTime(): Boolean =
        dataStore.data.map { preferences ->
            preferences[IS_FIRST_TIME] ?: true
        }.first()

    suspend fun setNotFirstTime() {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_TIME] = false
        }
    }
}