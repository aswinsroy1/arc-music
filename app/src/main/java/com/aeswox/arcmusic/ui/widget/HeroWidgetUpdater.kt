package com.aeswox.arcmusic.ui.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object HeroWidgetUpdater {
    val KEY_IS_PLAYING = booleanPreferencesKey("is_playing")
    val KEY_TRACK_TITLE = stringPreferencesKey("track_title")
    val KEY_TRACK_ARTIST = stringPreferencesKey("track_artist")
    val KEY_TRACK_ART = stringPreferencesKey("track_art")
    val KEY_CURRENT_LYRIC = stringPreferencesKey("current_lyric")

    suspend fun updateWidgetState(
        context: Context,
        isPlaying: Boolean,
        trackTitle: String,
        trackArtist: String,
        trackArt: String,
        currentLyric: String
    ) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(HeroWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[KEY_IS_PLAYING] = isPlaying
                    this[KEY_TRACK_TITLE] = trackTitle
                    this[KEY_TRACK_ARTIST] = trackArtist
                    this[KEY_TRACK_ART] = trackArt
                    this[KEY_CURRENT_LYRIC] = currentLyric
                }
            }
        }
        HeroWidget().updateAll(context)
    }
}
