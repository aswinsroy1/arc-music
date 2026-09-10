package com.aeswox.arcmusic.playback

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
        private const val TAG = "EqualizerManager"
        const val NUM_UI_BANDS = 10
        // Standard Android EQ range is ±15 dB (±1500 mB on most devices)
        const val MIN_LEVEL = -15
        const val MAX_LEVEL = 15
        const val MAX_LOUDNESS_GAIN_MB = 1000
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("EqualizerPrefs", Context.MODE_PRIVATE)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var currentAudioSessionId: Int = 0

    // Actual device millibel range – read from hardware on attach
    private var minEqLevel: Short = -1500
    private var maxEqLevel: Short = 1500

    // Global device capability flags – queried once at init
    private var isBassBoostSupportedGlobal = false
    private var isVirtualizerSupportedGlobal = false
    // LoudnessEnhancer is always available on API 19+
    private val isLoudnessSupportedGlobal = true

    // ---------- State flows ----------

    private val _isEnabled = MutableStateFlow(prefs.getBoolean("eq_enabled", false))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _bandLevels = MutableStateFlow(List(NUM_UI_BANDS) { i ->
        prefs.getInt("eq_band_$i", 0)
    })
    val bandLevels: StateFlow<List<Int>> = _bandLevels.asStateFlow()

    private val _bassBoostEnabled = MutableStateFlow(prefs.getBoolean("bb_enabled", false))
    val bassBoostEnabled: StateFlow<Boolean> = _bassBoostEnabled.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(prefs.getInt("bb_strength", 500))
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _virtualizerEnabled = MutableStateFlow(prefs.getBoolean("virt_enabled", false))
    val virtualizerEnabled: StateFlow<Boolean> = _virtualizerEnabled.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(prefs.getInt("virt_strength", 500))
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    private val _loudnessEnabled = MutableStateFlow(prefs.getBoolean("loudness_enabled", false))
    val loudnessEnabled: StateFlow<Boolean> = _loudnessEnabled.asStateFlow()

    private val _loudnessStrength = MutableStateFlow(prefs.getInt("loudness_strength", 300))
    val loudnessStrength: StateFlow<Int> = _loudnessStrength.asStateFlow()

    val isAttached: Boolean get() = equalizer != null && currentAudioSessionId != 0

    init {
        checkDeviceSupport()
    }

    private fun checkDeviceSupport() {
        try {
            val effects = AudioEffect.queryEffects() ?: return
            isBassBoostSupportedGlobal = effects.any {
                it.type == AudioEffect.EFFECT_TYPE_BASS_BOOST
            }
            isVirtualizerSupportedGlobal = effects.any {
                it.type == AudioEffect.EFFECT_TYPE_VIRTUALIZER
            }
            Log.d(TAG, "Device support – BassBoost: $isBassBoostSupportedGlobal, Virtualizer: $isVirtualizerSupportedGlobal")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query audio effects", e)
        }
    }

    // ---------- Session attachment ----------

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentAudioSessionId) return

        release()

        try {
            // Priority 0 is the recommended standard value. Using Int.MAX_VALUE can conflict
            // with system OEM effects on some devices.
            equalizer = Equalizer(0, audioSessionId).apply {
                minEqLevel = bandLevelRange[0]
                maxEqLevel = bandLevelRange[1]
                enabled = _isEnabled.value
            }
            currentAudioSessionId = audioSessionId
            Log.d(TAG, "EQ attached to session $audioSessionId – range: $minEqLevel to $maxEqLevel mB")

            applyBandLevels(_bandLevels.value)

            if (isBassBoostSupportedGlobal) {
                try {
                    bassBoost = BassBoost(0, audioSessionId).apply {
                        if (strengthSupported) setStrength(_bassBoostStrength.value.toShort())
                        enabled = _bassBoostEnabled.value
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "BassBoost unavailable: ${e.message}")
                    isBassBoostSupportedGlobal = false
                }
            }

            if (isVirtualizerSupportedGlobal) {
                try {
                    virtualizer = Virtualizer(0, audioSessionId).apply {
                        if (strengthSupported) setStrength(_virtualizerStrength.value.toShort())
                        enabled = _virtualizerEnabled.value
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Virtualizer unavailable: ${e.message}")
                    isVirtualizerSupportedGlobal = false
                }
            }

            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    setTargetGain(_loudnessStrength.value.coerceIn(0, MAX_LOUDNESS_GAIN_MB))
                    enabled = _loudnessEnabled.value
                }
            } catch (e: Exception) {
                Log.w(TAG, "LoudnessEnhancer unavailable: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize audio effects for session $audioSessionId", e)
            release()
        }
    }

    // ---------- EQ controls ----------

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean("eq_enabled", enabled).apply()
        try {
            equalizer?.enabled = enabled
            if (enabled) applyBandLevels(_bandLevels.value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set EQ enabled state", e)
        }
    }

    fun setBandLevel(uiBandIndex: Int, level: Int) {
        if (uiBandIndex !in 0 until NUM_UI_BANDS) return

        val clamped = level.coerceIn(MIN_LEVEL, MAX_LEVEL)
        val newLevels = _bandLevels.value.toMutableList()
        newLevels[uiBandIndex] = clamped
        _bandLevels.value = newLevels

        prefs.edit().putInt("eq_band_$uiBandIndex", clamped).apply()
        applyBandLevels(newLevels)
    }

    fun applyPreset(levels: List<Int>) {
        if (levels.size != NUM_UI_BANDS) return
        val clamped = levels.map { it.coerceIn(MIN_LEVEL, MAX_LEVEL) }
        _bandLevels.value = clamped

        val editor = prefs.edit()
        clamped.forEachIndexed { i, v -> editor.putInt("eq_band_$i", v) }
        editor.apply()

        applyBandLevels(clamped)
    }

    private fun applyBandLevels(uiLevels: List<Int>) {
        val eq = equalizer ?: return
        val deviceBandCount = eq.numberOfBands.toInt()
        if (deviceBandCount <= 0) return

        if (deviceBandCount >= NUM_UI_BANDS) {
            // Device has same or more bands – apply 1:1
            uiLevels.forEachIndexed { index, level ->
                applyBandLevelDirect(index, level)
            }
        } else {
            // Device has fewer bands (e.g. 5 bands) – average UI bands into device bands
            val ratio = NUM_UI_BANDS.toFloat() / deviceBandCount.toFloat()
            for (deviceBand in 0 until deviceBandCount) {
                val startUi = (deviceBand * ratio).toInt()
                val endUi = ((deviceBand + 1) * ratio).toInt().coerceAtMost(NUM_UI_BANDS)

                var sum = 0; var count = 0
                for (ui in startUi until endUi) {
                    if (ui < uiLevels.size) { sum += uiLevels[ui]; count++ }
                }
                applyBandLevelDirect(deviceBand, if (count > 0) sum / count else 0)
            }
        }
    }

    private fun applyBandLevelDirect(bandIndex: Int, normalizedLevel: Int) {
        val eq = equalizer ?: return
        if (bandIndex >= eq.numberOfBands) return

        // Convert normalized ±15 dB to device millibel range
        // Formula: map [-15, +15] → [minEqLevel, maxEqLevel]
        val range = maxEqLevel - minEqLevel
        val millibelLevel = (minEqLevel + (normalizedLevel + 15) * range / 30).toShort()

        try {
            eq.setBandLevel(bandIndex.toShort(), millibelLevel)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set band $bandIndex level: ${e.message}")
        }
    }

    // ---------- Bass Boost ----------

    fun setBassBoostEnabled(enabled: Boolean) {
        if (!isBassBoostSupportedGlobal) return
        _bassBoostEnabled.value = enabled
        prefs.edit().putBoolean("bb_enabled", enabled).apply()
        try { bassBoost?.enabled = enabled }
        catch (e: Exception) { Log.e(TAG, "Failed to set BassBoost enabled", e) }
    }

    fun setBassBoostStrength(strength: Int) {
        if (!isBassBoostSupportedGlobal) return
        val clamped = strength.coerceIn(0, 1000)
        _bassBoostStrength.value = clamped
        prefs.edit().putInt("bb_strength", clamped).apply()
        try {
            bassBoost?.apply { if (strengthSupported) setStrength(clamped.toShort()) }
        } catch (e: Exception) { Log.e(TAG, "Failed to set BassBoost strength", e) }
    }

    // ---------- Virtualizer ----------

    fun setVirtualizerEnabled(enabled: Boolean) {
        if (!isVirtualizerSupportedGlobal) return
        _virtualizerEnabled.value = enabled
        prefs.edit().putBoolean("virt_enabled", enabled).apply()
        try { virtualizer?.enabled = enabled }
        catch (e: Exception) { Log.e(TAG, "Failed to set Virtualizer enabled", e) }
    }

    fun setVirtualizerStrength(strength: Int) {
        if (!isVirtualizerSupportedGlobal) return
        val clamped = strength.coerceIn(0, 1000)
        _virtualizerStrength.value = clamped
        prefs.edit().putInt("virt_strength", clamped).apply()
        try {
            virtualizer?.apply { if (strengthSupported) setStrength(clamped.toShort()) }
        } catch (e: Exception) { Log.e(TAG, "Failed to set Virtualizer strength", e) }
    }

    // ---------- Loudness Enhancer ----------

    fun setLoudnessEnabled(enabled: Boolean) {
        _loudnessEnabled.value = enabled
        prefs.edit().putBoolean("loudness_enabled", enabled).apply()
        try { loudnessEnhancer?.enabled = enabled }
        catch (e: Exception) { Log.e(TAG, "Failed to set LoudnessEnhancer enabled", e) }
    }

    fun setLoudnessStrength(strength: Int) {
        val clamped = strength.coerceIn(0, MAX_LOUDNESS_GAIN_MB)
        _loudnessStrength.value = clamped
        prefs.edit().putInt("loudness_strength", clamped).apply()
        try { loudnessEnhancer?.setTargetGain(clamped) }
        catch (e: Exception) { Log.e(TAG, "Failed to set LoudnessEnhancer strength", e) }
    }

    // ---------- Device capability queries ----------

    fun isBassBoostSupported(): Boolean = isBassBoostSupportedGlobal
    fun isVirtualizerSupported(): Boolean = isVirtualizerSupportedGlobal
    fun isLoudnessEnhancerSupported(): Boolean = isLoudnessSupportedGlobal

    // ---------- Release ----------

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio effects", e)
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        currentAudioSessionId = 0
    }
}
