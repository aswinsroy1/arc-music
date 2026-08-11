package com.aeswox.arcmusic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aeswox.arcmusic.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val LAST_FM_KEY   = stringPreferencesKey("last_fm_api_key")
    private val FANART_TV_KEY = stringPreferencesKey("fanart_tv_api_key")
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    val lastFmApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_FM_KEY]
    }

    val fanartTvApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[FANART_TV_KEY]
    }

    /** Persisted theme preference. Emits [ThemeMode.Light] by default (first-run). */
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_MODE_KEY]) {
            "dark"   -> ThemeMode.Dark
            "system" -> ThemeMode.System
            else     -> ThemeMode.Light
        }
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

    suspend fun setFanartTvApiKey(key: String) {
        context.dataStore.edit { preferences ->
            if (key.isBlank()) {
                preferences.remove(FANART_TV_KEY)
            } else {
                preferences[FANART_TV_KEY] = key.trim()
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = when (mode) {
                ThemeMode.Dark   -> "dark"
                ThemeMode.System -> "system"
                ThemeMode.Light  -> "light"
            }
        }
    }
}
