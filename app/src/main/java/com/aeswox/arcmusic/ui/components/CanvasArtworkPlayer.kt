package com.aeswox.arcmusic.ui.components

import android.view.TextureView
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isEmpty
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.ui.AspectRatioFrameLayout
import java.util.Locale

/**
 * Renders a looping, muted video canvas artwork (Spotify Canvas style).
 * Fades in once the first frame is rendered to avoid a jarring flash.
 * Pauses/resumes automatically with [isPlaying].
 */
@OptIn(UnstableApi::class)
@Composable
fun CanvasArtworkPlayer(
    url: String,
    isPlaying: Boolean,
    cacheDataSourceFactory: CacheDataSource.Factory? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isVideoReady by remember(url) { mutableStateOf(false) }
    var firstFrameRendered by remember(url) { mutableStateOf(false) }

    LaunchedEffect(firstFrameRendered) {
        if (firstFrameRendered) {
            kotlinx.coroutines.delay(100)
            isVideoReady = true
        }
    }
    val currentIsPlaying by rememberUpdatedState(isPlaying)

    val exoPlayer = remember {
        val builder = ExoPlayer.Builder(context.applicationContext)
        if (cacheDataSourceFactory != null) {
            builder.setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
        }
        builder.build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus= */ false,
            )
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    // Sync play/pause
    LaunchedEffect(isPlaying) {
        exoPlayer.playWhenReady = isPlaying
    }

    // Listen for first frame and errors
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                firstFrameRendered = true
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("CanvasArtworkPlayer", "ExoPlayer Error: ${error.errorCodeName}", error)
                firstFrameRendered = false
                isVideoReady = false
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Load media
    LaunchedEffect(url) {
        val normalized = url.trim()
        val isM3u8 = normalized.contains(".m3u8", ignoreCase = true) ||
            normalized.contains("apple.com") || normalized.contains("itunes.apple")
        val mimeType = when {
            isM3u8 -> MimeTypes.APPLICATION_M3U8
            normalized.lowercase(Locale.ROOT).contains(".mp4") -> MimeTypes.VIDEO_MP4
            else -> MimeTypes.APPLICATION_M3U8
        }
        firstFrameRendered = false
        isVideoReady = false
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(
            MediaItem.Builder().setUri(normalized).setMimeType(mimeType).build()
        )
        exoPlayer.prepare()
        exoPlayer.playWhenReady = currentIsPlaying
    }

    // Release player when composable leaves
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = if (isVideoReady) tween(durationMillis = 600) else androidx.compose.animation.core.snap(),
        label = "canvasAlpha"
    )

    AndroidView(
        modifier = modifier.alpha(alpha),
        factory = { ctx ->
            AspectRatioFrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setAspectRatio(1f)
                if (childCount == 0) {
                    val textureView = TextureView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    }
                    addView(textureView)
                    exoPlayer.setVideoTextureView(textureView)
                }
            }
        },
        update = { /* player is managed by LaunchedEffect; no update needed */ }
    )
}
