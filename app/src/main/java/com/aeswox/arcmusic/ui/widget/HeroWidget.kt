package com.aeswox.arcmusic.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import androidx.glance.Image
import androidx.glance.layout.ContentScale
import androidx.datastore.preferences.core.Preferences
import com.aeswox.arcmusic.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.aeswox.arcmusic.playback.MusicPlayerConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeroWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            HeroWidgetContent()
        }
    }
}

@Composable
fun HeroWidgetContent() {
    val prefs = currentState<Preferences>()
    val isPlaying = prefs[HeroWidgetUpdater.KEY_IS_PLAYING] ?: false
    val trackTitle = prefs[HeroWidgetUpdater.KEY_TRACK_TITLE] ?: "Arc Music"
    val trackArtist = prefs[HeroWidgetUpdater.KEY_TRACK_ARTIST] ?: "Your Music Companion"
    val trackArt = prefs[HeroWidgetUpdater.KEY_TRACK_ART] ?: ""
    val currentLyric = prefs[HeroWidgetUpdater.KEY_CURRENT_LYRIC] ?: ""

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(android.graphics.Color.DKGRAY)) // Fallback background
    ) {
        // In a real widget, you would load the trackArt image from a file URI into a Bitmap.
        // For simplicity and battery preservation in Glance, we'll use a placeholder or gradient.
        
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            if (isPlaying && currentLyric.isNotBlank()) {
                Text(
                    text = currentLyric,
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.WHITE),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )
            }
            
            Text(
                text = trackTitle,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.WHITE),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = trackArtist,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.LTGRAY),
                    fontSize = 14.sp
                ),
                modifier = GlanceModifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_skip_previous), // Ensure this exists, else use standard icon
                    contentDescription = "Previous",
                    modifier = GlanceModifier.size(48.dp).clickable(actionRunCallback<SkipPrevAction>())
                )
                Spacer(modifier = GlanceModifier.size(16.dp))
                Image(
                    provider = ImageProvider(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                    contentDescription = "Play/Pause",
                    modifier = GlanceModifier.size(64.dp).clickable(actionRunCallback<TogglePlayAction>())
                )
                Spacer(modifier = GlanceModifier.size(16.dp))
                Image(
                    provider = ImageProvider(R.drawable.ic_skip_next),
                    contentDescription = "Next",
                    modifier = GlanceModifier.size(48.dp).clickable(actionRunCallback<SkipNextAction>())
                )
            }
        }
    }
}

// Action Callbacks
class TogglePlayAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val connection = entryPoint.musicPlayerConnection()
        CoroutineScope(Dispatchers.Main).launch {
            if (connection.isPlaying.value) {
                connection.pause()
            } else {
                connection.play()
            }
        }
    }
}

class SkipNextAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        CoroutineScope(Dispatchers.Main).launch {
            entryPoint.musicPlayerConnection().skipToNext()
        }
    }
}

class SkipPrevAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        CoroutineScope(Dispatchers.Main).launch {
            entryPoint.musicPlayerConnection().skipToPrevious()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun musicPlayerConnection(): MusicPlayerConnection
}
