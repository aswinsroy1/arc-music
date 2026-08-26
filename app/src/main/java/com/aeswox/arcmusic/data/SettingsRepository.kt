package com.aeswox.arcmusic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aeswox.arcmusic.ThemeMode
import com.aeswox.arcmusic.data.model.LyricsDisplayStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.booleanPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val DEFAULT_EXCLUDED_FOLDERS = listOf(
            "/Android/media",
            "/Android/data",
            "/Downloads/VoiceNotes",
            "/WhatsApp/Media/WhatsApp Audio",
            "/WhatsApp/Media/WhatsApp Voice Notes",
            "/WhatsApp/Media/Sent"
        )
    }
    private val LAST_FM_KEY   = stringPreferencesKey("last_fm_api_key")
    private val FANART_TV_KEY = stringPreferencesKey("fanart_tv_api_key")
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val TINT_TRANSPARENCY_KEY = floatPreferencesKey("tint_transparency")
    private val NOISE_FACTOR_KEY = floatPreferencesKey("noise_factor")
    private val GLOW_INTENSITY_KEY = floatPreferencesKey("glow_intensity")
    private val MIN_SONG_DURATION_KEY = intPreferencesKey("min_song_duration_sec")
    private val MIN_TRACKS_PER_ALBUM_KEY = intPreferencesKey("min_tracks_per_album")
    // Stored as pipe-separated string e.g. "/Android/media|/Downloads/VoiceNotes"
    private val EXCLUDED_FOLDERS_KEY = stringPreferencesKey("excluded_folders")
    private val LIGHT_THEME_NOW_PLAYING_KEY = stringPreferencesKey("light_theme_now_playing")
    private val COIL_DISK_CACHE_LIMIT_MB_KEY = intPreferencesKey("coil_disk_cache_limit_mb")
    private val LYRICS_DISPLAY_STYLE_KEY = stringPreferencesKey("lyrics_display_style")
    private val LYRICS_SHOW_CONTROLS_KEY = booleanPreferencesKey("lyrics_show_controls")
    private val LYRICS_FADE_STEEPNESS_KEY = floatPreferencesKey("lyrics_fade_steepness")
    private val LYRICS_FADE_SCALE_CEILING_KEY = floatPreferencesKey("lyrics_fade_scale_ceiling")
    private val LYRICS_FADE_DISTANCE_SIZING_KEY = booleanPreferencesKey("lyrics_fade_distance_sizing")
    private val LYRICS_BLUR_RADIUS_KEY = floatPreferencesKey("lyrics_blur_radius")
    private val LYRICS_BLUR_DIMMING_KEY = floatPreferencesKey("lyrics_blur_dimming")
    private val CANVAS_ENABLED_KEY = booleanPreferencesKey("canvas_enabled")
    private val CANVAS_CACHE_LIMIT_MB_KEY = intPreferencesKey("canvas_cache_limit_mb")

    private val MASS_KEY = floatPreferencesKey("physics_mass")
    private val STIFFNESS_KEY = floatPreferencesKey("physics_stiffness")
    private val DAMPING_RATIO_KEY = floatPreferencesKey("physics_damping_ratio")
    private val AMPLITUDE_KEY = floatPreferencesKey("physics_amplitude")
    private val GRAVITY_KEY = floatPreferencesKey("physics_gravity")

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

    /**
     * Persisted lyrics display style. Emits [LyricsDisplayStyle.FADE] by default
     * (first-run and any unrecognised value), so new installs and users who have
     * not yet chosen get the lighter FADE style automatically.
     */
    val lyricsDisplayStyle: Flow<LyricsDisplayStyle> = context.dataStore.data.map { preferences ->
        when (preferences[LYRICS_DISPLAY_STYLE_KEY]) {
            "distance_blur" -> LyricsDisplayStyle.DISTANCE_BLUR
            else            -> LyricsDisplayStyle.FADE
        }
    }

    val lyricsShowControls: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LYRICS_SHOW_CONTROLS_KEY] ?: true
    }
    
    val lyricsFadeSteepness: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LYRICS_FADE_STEEPNESS_KEY] ?: 1.2f
    }
    
    val lyricsFadeScaleCeiling: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LYRICS_FADE_SCALE_CEILING_KEY] ?: 0.85f
    }
    
    val lyricsFadeDistanceSizing: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LYRICS_FADE_DISTANCE_SIZING_KEY] ?: true
    }
    
    val lyricsBlurRadius: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LYRICS_BLUR_RADIUS_KEY] ?: 10f
    }

    val lyricsBlurDimming: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[LYRICS_BLUR_DIMMING_KEY] ?: 0.28f
    }

    val canvasEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CANVAS_ENABLED_KEY] ?: true
    }

    val canvasCacheLimitMb: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CANVAS_CACHE_LIMIT_MB_KEY] ?: 250
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
    
    val physicsMass: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[MASS_KEY] ?: 0.2f
    }
    
    val physicsStiffness: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[STIFFNESS_KEY] ?: 100.0f
    }
    
    val physicsDampingRatio: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[DAMPING_RATIO_KEY] ?: 0.25f
    }
    
    val physicsAmplitude: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[AMPLITUDE_KEY] ?: 1.0f
    }
    
    val physicsGravity: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[GRAVITY_KEY] ?: 9.81f
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
        context.dataStore.edit { preferences ->
            preferences[TINT_TRANSPARENCY_KEY] = value
        }
    }
    
    suspend fun setNoiseFactor(value: Float) {
        context.dataStore.edit { preferences -> preferences[NOISE_FACTOR_KEY] = value }
    }
    
    suspend fun setGlowIntensity(value: Float) {
        context.dataStore.edit { preferences -> preferences[GLOW_INTENSITY_KEY] = value }
    }
    
    suspend fun setPhysicsMass(value: Float) {
        context.dataStore.edit { preferences -> preferences[MASS_KEY] = value }
    }
    
    suspend fun setPhysicsStiffness(value: Float) {
        context.dataStore.edit { preferences -> preferences[STIFFNESS_KEY] = value }
    }
    
    suspend fun setPhysicsDampingRatio(value: Float) {
        context.dataStore.edit { preferences -> preferences[DAMPING_RATIO_KEY] = value }
    }
    
    suspend fun setPhysicsAmplitude(value: Float) {
        context.dataStore.edit { preferences -> preferences[AMPLITUDE_KEY] = value }
    }
    
    suspend fun setPhysicsGravity(value: Float) {
        context.dataStore.edit { preferences -> preferences[GRAVITY_KEY] = value }
    }

    // ------- Media Management Prefs -------

    val minSongDurationSec: Flow<Int> = context.dataStore.data.map { it[MIN_SONG_DURATION_KEY] ?: 0 }
    val minTracksPerAlbum: Flow<Int> = context.dataStore.data.map { it[MIN_TRACKS_PER_ALBUM_KEY] ?: 1 }
    val excludedFolders: Flow<List<String>> = context.dataStore.data.map { prefs ->
        if (prefs.contains(EXCLUDED_FOLDERS_KEY)) {
            prefs[EXCLUDED_FOLDERS_KEY]?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
        } else {
            DEFAULT_EXCLUDED_FOLDERS
        }
    }
    val lightThemeForNowPlaying: Flow<Boolean> = context.dataStore.data.map {
        it[LIGHT_THEME_NOW_PLAYING_KEY] == "true"
    }
    
    val coilDiskCacheLimitMb: Flow<Int> = context.dataStore.data.map {
        it[COIL_DISK_CACHE_LIMIT_MB_KEY] ?: 250 // default 250MB
    }

    suspend fun setMinSongDurationSec(value: Int) {
        context.dataStore.edit { it[MIN_SONG_DURATION_KEY] = value }
    }

    suspend fun setMinTracksPerAlbum(value: Int) {
        context.dataStore.edit { it[MIN_TRACKS_PER_ALBUM_KEY] = value }
    }

    suspend fun setExcludedFolders(folders: List<String>) {
        context.dataStore.edit { it[EXCLUDED_FOLDERS_KEY] = folders.joinToString("|") }
    }

    suspend fun setLightThemeForNowPlaying(value: Boolean) {
        context.dataStore.edit { it[LIGHT_THEME_NOW_PLAYING_KEY] = value.toString() }
    }
    
    suspend fun setCoilDiskCacheLimitMb(value: Int) {
        context.dataStore.edit { it[COIL_DISK_CACHE_LIMIT_MB_KEY] = value }
    }

    suspend fun setLyricsDisplayStyle(style: LyricsDisplayStyle) {
        context.dataStore.edit {
            it[LYRICS_DISPLAY_STYLE_KEY] = when (style) {
                LyricsDisplayStyle.FADE          -> "fade"
                LyricsDisplayStyle.DISTANCE_BLUR -> "distance_blur"
            }
        }
    }

    suspend fun setLyricsShowControls(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[LYRICS_SHOW_CONTROLS_KEY] = show }
    }

    suspend fun setLyricsFadeSteepness(steepness: Float) {
        context.dataStore.edit { preferences -> preferences[LYRICS_FADE_STEEPNESS_KEY] = steepness }
    }

    suspend fun setLyricsFadeScaleCeiling(ceiling: Float) {
        context.dataStore.edit { preferences -> preferences[LYRICS_FADE_SCALE_CEILING_KEY] = ceiling }
    }

    suspend fun setLyricsFadeDistanceSizing(enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[LYRICS_FADE_DISTANCE_SIZING_KEY] = enabled }
    }

    suspend fun setLyricsBlurRadius(radius: Float) {
        context.dataStore.edit { preferences -> preferences[LYRICS_BLUR_RADIUS_KEY] = radius }
    }

    suspend fun setLyricsBlurDimming(dimming: Float) {
        context.dataStore.edit { preferences -> preferences[LYRICS_BLUR_DIMMING_KEY] = dimming }
    }

    suspend fun setCanvasEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CANVAS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setCanvasCacheLimitMb(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[CANVAS_CACHE_LIMIT_MB_KEY] = limit
        }
    }
}
