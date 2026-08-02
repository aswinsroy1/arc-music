package com.aeswox.arcmusic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val LAST_FM_KEY = stringPreferencesKey("last_fm_api_key")

    val lastFmApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_FM_KEY]
    }

    suspend fun setLastFmApiKey(key: String) {
        context.dataStore.edit { preferences ->
            if (key.isBlank()) {
                preferences.remove(LAST_FM_KEY)
            } else {
                preferences[LAST_FM_KEY] = key.trim()
            }
        }
    }
}
