package com.aeswox.arcmusic.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class SleepTimerManager(
    private val scope: CoroutineScope,
    private val player: Player,
) : Player.Listener {

    private var sleepTimerJob: Job? = null

    private val _triggerTime = MutableStateFlow(-1L)
    val triggerTime: StateFlow<Long> = _triggerTime.asStateFlow()

    private val _pauseWhenSongEnd = MutableStateFlow(false)
    val pauseWhenSongEnd: StateFlow<Boolean> = _pauseWhenSongEnd.asStateFlow()

    val isActive: Boolean
        get() = _triggerTime.value != -1L || _pauseWhenSongEnd.value

    init {
        player.addListener(this)
    }

    fun start(minute: Int) {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        if (minute == -1) {
            _pauseWhenSongEnd.value = true
            _triggerTime.value = -1L
        } else {
            _pauseWhenSongEnd.value = false
            _triggerTime.value = System.currentTimeMillis() + minute.minutes.inWholeMilliseconds
            sleepTimerJob = scope.launch {
                val delayTime = _triggerTime.value - System.currentTimeMillis()
                if (delayTime > 0) {
                    delay(delayTime)
                }
                fadeOutAndPause()
                _triggerTime.value = -1L
            }
        }
    }

    private suspend fun fadeOutAndPause() {
        val initialVolume = player.volume
        val fadeDuration = 3000L
        val steps = 30
        val stepDelay = fadeDuration / steps
        for (i in steps downTo 1) {
            player.volume = initialVolume * (i.toFloat() / steps)
            delay(stepDelay)
        }
        player.pause()
        player.volume = initialVolume
    }

    fun clear() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _pauseWhenSongEnd.value = false
        _triggerTime.value = -1L
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (_pauseWhenSongEnd.value && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            _pauseWhenSongEnd.value = false
            player.pause()
        }
    }

    override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
        if (playbackState == Player.STATE_ENDED && _pauseWhenSongEnd.value) {
            _pauseWhenSongEnd.value = false
            player.pause()
        }
    }
}
