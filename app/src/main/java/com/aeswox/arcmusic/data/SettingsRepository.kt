package com.aeswox.arcmusic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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
    private val TINT_TRANSPARENCY_KEY = floatPreferencesKey("tint_transparency")
    private val NOISE_FACTOR_KEY = floatPreferencesKey("noise_factor")
    private val GLOW_INTENSITY_KEY = floatPreferencesKey("glow_intensity")

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

    val tintTransparency: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TINT_TRANSPARENCY_KEY] ?: 0.4f
    }
    
    val noiseFactor: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[NOISE_FACTOR_KEY] ?: 0.06f
    }
    
    val glowIntensity: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[GLOW_INTENSITY_KEY] ?: 0.6f
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

    suspend fun setTintTransparency(value: Float) {
        context.dataStore.edit { preferences -> preferences[TINT_TRANSPARENCY_KEY] = value }
    }
    
    suspend fun setNoiseFactor(value: Float) {
        context.dataStore.edit { preferences -> preferences[NOISE_FACTOR_KEY] = value }
    }
    
    suspend fun setGlowIntensity(value: Float) {
        context.dataStore.edit { preferences -> preferences[GLOW_INTENSITY_KEY] = value }
    }
}
