package com.aeswox.arcmusic.playback

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aeswox.arcmusic.db.MusicRepository
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository
) {
    private var controllerFuture: ListenableFuture<MediaController>
    private var mediaController: MediaController? = null

    var sleepTimerManager: SleepTimerManager? = null
        private set

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentlyPlayingItem = MutableStateFlow<MediaItem?>(null)
    val currentlyPlayingItem: StateFlow<MediaItem?> = _currentlyPlayingItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentQueue: StateFlow<List<MediaItem>> = _currentQueue.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(-1)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _sleepTimerTriggerTime = MutableStateFlow(-1L)
    val sleepTimerTriggerTime: StateFlow<Long> = _sleepTimerTriggerTime.asStateFlow()

    private val _sleepTimerPauseWhenSongEnd = MutableStateFlow(false)
    val sleepTimerPauseWhenSongEnd: StateFlow<Boolean> = _sleepTimerPauseWhenSongEnd.asStateFlow()

    private val _deviceVolume = MutableStateFlow(0)
    val deviceVolume: StateFlow<Int> = _deviceVolume.asStateFlow()

    private val _deviceMaxVolume = MutableStateFlow(15)
    val deviceMaxVolume: StateFlow<Int> = _deviceMaxVolume.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Tracks the media ID we last logged a play-start for, to avoid double-logging
    private var lastLoggedMediaId: String? = null
    // Wall-clock ms when the track last entered the PLAYING state
    private var lastPlayStateChangeTimeMs: Long = 0L
    // Accumulated listening time across pauses and seeks for the current track
    private var accumulatedPlayTimeMs: Long = 0L

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            mediaController = controller
            mediaController?.addListener(PlayerListener())
            
            val timerManager = SleepTimerManager(scope, controller)
            sleepTimerManager = timerManager
            
            // Wire state flows
            scope.launch {
                timerManager.triggerTime.collect { _sleepTimerTriggerTime.value = it }
            }
            scope.launch {
                timerManager.pauseWhenSongEnd.collect { _sleepTimerPauseWhenSongEnd.value = it }
            }

            // Sync initial state
            mediaController?.let {
                _isPlaying.value = it.isPlaying
                _currentlyPlayingItem.value = it.currentMediaItem
                _currentMediaItemIndex.value = it.currentMediaItemIndex
                val items = mutableListOf<MediaItem>()
                for (i in 0 until it.mediaItemCount) {
                    items.add(it.getMediaItemAt(i))
                }
                _currentQueue.value = items
                
                _deviceMaxVolume.value = it.deviceInfo.maxVolume
                _deviceVolume.value = it.deviceVolume
            }
            scope.launch {
                while (isActive) {
                    mediaController?.let { controller ->
                        if (controller.isPlaying) {
                            _currentPosition.value = controller.currentPosition
                            val dur = controller.duration
                            _duration.value = if (dur < 0) 0L else dur
                        }
                    }
                    delay(32)
                }
            }
        }, MoreExecutors.directExecutor())
    }

    private inner class PlayerListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _playbackState.value = when (playbackState) {
                Player.STATE_IDLE -> PlaybackState.IDLE
                Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                Player.STATE_READY -> PlaybackState.READY
                Player.STATE_ENDED -> PlaybackState.ENDED
                else -> PlaybackState.IDLE
            }

            // Natural track completion — mark the most recent history row as completed
            if (playbackState == Player.STATE_ENDED) {
                val trackId = mediaController?.currentMediaItem?.mediaId
                if (!trackId.isNullOrEmpty()) {
                    // Stop accumulating if we were playing
                    if (_isPlaying.value) {
                        val now = System.currentTimeMillis()
                        accumulatedPlayTimeMs += (now - lastPlayStateChangeTimeMs)
                        lastPlayStateChangeTimeMs = now
                    }
                    val playedMs = if (accumulatedPlayTimeMs > 0L)
                        accumulatedPlayTimeMs
                    else
                        (mediaController?.duration?.takeIf { it > 0 } ?: 0L)
                    Log.d("PlayHistory", "Track ended naturally, marking completed: $trackId, playedMs=$playedMs")
                    scope.launch(Dispatchers.IO) {
                        repository.markPlayCompleted(trackId, playedMs)
                    }
                    accumulatedPlayTimeMs = 0L
                    lastPlayStateChangeTimeMs = 0L
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val now = System.currentTimeMillis()
            if (isPlaying && !_isPlaying.value) {
                // Playback resumed or started
                lastPlayStateChangeTimeMs = now
            } else if (!isPlaying && _isPlaying.value) {
                // Playback paused
                if (lastPlayStateChangeTimeMs > 0L) {
                    accumulatedPlayTimeMs += (now - lastPlayStateChangeTimeMs)
                }
            }
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentlyPlayingItem.value = mediaItem
            _currentMediaItemIndex.value = mediaController?.currentMediaItemIndex ?: -1
            _currentPosition.value = 0L
            val dur = mediaController?.duration ?: 0L
            _duration.value = if (dur < 0) 0L else dur

            // Persist actual played time for the track we're leaving (skip / auto-advance before STATE_ENDED)
            val previousId = lastLoggedMediaId
            if (!previousId.isNullOrEmpty()) {
                // If we were playing right before the transition, add the final slice of time
                if (_isPlaying.value && lastPlayStateChangeTimeMs > 0L) {
                    accumulatedPlayTimeMs += (System.currentTimeMillis() - lastPlayStateChangeTimeMs)
                }
                if (accumulatedPlayTimeMs > 0L) {
                    scope.launch(Dispatchers.IO) {
                        repository.updatePlayedMs(previousId, accumulatedPlayTimeMs)
                    }
                }
            }

            // Log a new play-start for every distinct media item that begins playing
            val trackId = mediaItem?.mediaId
            if (!trackId.isNullOrEmpty() && trackId != lastLoggedMediaId) {
                lastLoggedMediaId = trackId
                accumulatedPlayTimeMs = 0L
                lastPlayStateChangeTimeMs = if (_isPlaying.value) System.currentTimeMillis() else 0L
                Log.d("PlayHistory", "Logging play start: trackId=$trackId, reason=$reason")
                scope.launch(Dispatchers.IO) {
                    repository.logPlayStart(trackId)
                }
            }
        }

        override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
            _deviceVolume.value = volume
        }

        override fun onDeviceInfoChanged(deviceInfo: androidx.media3.common.DeviceInfo) {
            _deviceMaxVolume.value = deviceInfo.maxVolume
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            mediaController?.let { controller ->
                val items = mutableListOf<MediaItem>()
                for (i in 0 until controller.mediaItemCount) {
                    items.add(controller.getMediaItemAt(i))
                }
                _currentQueue.value = items
                _currentMediaItemIndex.value = controller.currentMediaItemIndex
            }
        }
    }

    fun playTrack(mediaItem: MediaItem) {
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun playQueue(mediaItems: List<MediaItem>, startIndex: Int = 0) {
        mediaController?.setMediaItems(mediaItems, startIndex, 0)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun skipToQueueItem(index: Int) {
        mediaController?.seekToDefaultPosition(index)
    }

    fun seekTo(positionMs: Long) {
        _currentPosition.value = positionMs
        mediaController?.seekTo(positionMs)
    }

    fun setDeviceVolume(volume: Int) {
        mediaController?.deviceVolume = volume
        _deviceVolume.value = volume
    }

    fun release() {
        mediaController?.release()
    }
}

enum class PlaybackState {
    IDLE, BUFFERING, READY, ENDED
}

