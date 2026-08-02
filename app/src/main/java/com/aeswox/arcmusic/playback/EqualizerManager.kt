package com.aeswox.arcmusic.playback

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.Equalizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val NUM_UI_BANDS = 10
        const val MIN_LEVEL = -12
        const val MAX_LEVEL = 12
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("EqualizerPrefs", Context.MODE_PRIVATE)

    private var equalizer: Equalizer? = null
    private var currentAudioSessionId: Int = 0
    private var minEqLevel: Short = -1500
    private var maxEqLevel: Short = 1500

    private val _isEnabled = MutableStateFlow(prefs.getBoolean("eq_enabled", false))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _bandLevels = MutableStateFlow(List(NUM_UI_BANDS) { i ->
        prefs.getInt("eq_band_$i", 0)
    })
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentAudioSessionId) return
        
        release()
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                minEqLevel = bandLevelRange[0]
                maxEqLevel = bandLevelRange[1]
                enabled = _isEnabled.value
            }
            currentAudioSessionId = audioSessionId
            applyBandLevels(_bandLevels.value)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to initialize Equalizer for session $audioSessionId", e)
            release()
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean("eq_enabled", enabled).apply()
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to set equalizer enabled state", e)
        }
    }

    fun setBandLevel(uiBandIndex: Int, level: Int) {
        if (uiBandIndex !in 0 until NUM_UI_BANDS) return
        
        val clampedLevel = level.coerceIn(MIN_LEVEL, MAX_LEVEL)
        val newLevels = _bandLevels.value.toMutableList()
        newLevels[uiBandIndex] = clampedLevel
        _bandLevels.value = newLevels
        
        prefs.edit().putInt("eq_band_$uiBandIndex", clampedLevel).apply()
        
        applyBandLevels(newLevels)
    }

    fun applyPreset(levels: List<Int>) {
        if (levels.size != NUM_UI_BANDS) return
        val clampedLevels = levels.map { it.coerceIn(MIN_LEVEL, MAX_LEVEL) }
        _bandLevels.value = clampedLevels
        
        val editor = prefs.edit()
        clampedLevels.forEachIndexed { index, level ->
            editor.putInt("eq_band_$index", level)
        }
        editor.apply()
        
        applyBandLevels(clampedLevels)
    }

    private fun applyBandLevels(uiLevels: List<Int>) {
        val eq = equalizer ?: return
        val deviceBandCount = eq.numberOfBands.toInt()
        if (deviceBandCount <= 0) return

        if (deviceBandCount >= NUM_UI_BANDS) {
            uiLevels.forEachIndexed { index, level ->
                applyBandLevelDirect(index, level)
            }
        } else {
            val ratio = NUM_UI_BANDS.toFloat() / deviceBandCount.toFloat()
            for (deviceBand in 0 until deviceBandCount) {
                val startUiBand = (deviceBand * ratio).toInt()
                val endUiBand = ((deviceBand + 1) * ratio).toInt().coerceAtMost(NUM_UI_BANDS)
                
                var sum = 0
                var count = 0
                for (uiBand in startUiBand until endUiBand) {
                    if (uiBand < uiLevels.size) {
                        sum += uiLevels[uiBand]
                        count++
                    }
                }
                val averageLevel = if (count > 0) sum / count else 0
                applyBandLevelDirect(deviceBand, averageLevel)
            }
        }
    }

    private fun applyBandLevelDirect(bandIndex: Int, normalizedLevel: Int) {
        val eq = equalizer ?: return
        if (bandIndex >= eq.numberOfBands) return
        
        val range = maxEqLevel - minEqLevel
        // Transform normalized [-12, 12] to device range
        val millibelLevel = (minEqLevel + (normalizedLevel + 12) * range / 24).toShort()
        
        try {
            eq.setBandLevel(bandIndex.toShort(), millibelLevel)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to set band $bandIndex level", e)
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Error releasing Equalizer", e)
        }
        equalizer = null
        currentAudioSessionId = 0
    }
}
