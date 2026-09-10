package com.aeswox.arcmusic.playback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EqualizerUiState(
    val isEnabled: Boolean = false,
    val bandLevels: List<Int> = List(10) { 0 },
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 500,
    val isBassBoostSupported: Boolean = false,
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 500,
    val isVirtualizerSupported: Boolean = false,
    val loudnessEnabled: Boolean = false,
    val loudnessStrength: Int = 300,
    val isLoudnessEnhancerSupported: Boolean = true
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerManager: EqualizerManager
) : ViewModel() {

    companion object {
        private const val DEBOUNCE_MS = 100L
    }

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    // Keep these for backwards compat with EqualizerScreen
    val isEnabled get() = equalizerManager.isEnabled
    val bandLevels get() = equalizerManager.bandLevels

    private var bandDebounceJob: Job? = null
    private var bbStrengthJob: Job? = null
    private var virtStrengthJob: Job? = null
    private var loudnessStrengthJob: Job? = null

    init {
        syncStateFromManager()
    }

    private fun syncStateFromManager() {
        viewModelScope.launch {
            equalizerManager.isEnabled.collect { v ->
                _uiState.value = _uiState.value.copy(isEnabled = v)
            }
        }
        viewModelScope.launch {
            equalizerManager.bandLevels.collect { v ->
                _uiState.value = _uiState.value.copy(bandLevels = v)
            }
        }
        viewModelScope.launch {
            equalizerManager.bassBoostEnabled.collect { v ->
                _uiState.value = _uiState.value.copy(bassBoostEnabled = v)
            }
        }
        viewModelScope.launch {
            equalizerManager.bassBoostStrength.collect { v ->
                _uiState.value = _uiState.value.copy(bassBoostStrength = v)
            }
        }
        viewModelScope.launch {
            equalizerManager.virtualizerEnabled.collect { v ->
                _uiState.value = _uiState.value.copy(virtualizerEnabled = v)
            }
        }
        viewModelScope.launch {
            equalizerManager.virtualizerStrength.collect { v ->
                _uiState.value = _uiState.value.copy(virtualizerStrength = v)
            }
        }
        viewModelScope.launch {
            equalizerManager.loudnessEnabled.collect { v ->
                _uiState.value = _uiState.value.copy(loudnessEnabled = v)
            }
        }
        viewModelScope.launch {
            equalizerManager.loudnessStrength.collect { v ->
                _uiState.value = _uiState.value.copy(loudnessStrength = v)
            }
        }

        // Update device capability flags
        _uiState.value = _uiState.value.copy(
            isBassBoostSupported = equalizerManager.isBassBoostSupported(),
            isVirtualizerSupported = equalizerManager.isVirtualizerSupported(),
            isLoudnessEnhancerSupported = equalizerManager.isLoudnessEnhancerSupported()
        )
    }

    // ---------- EQ ----------

    fun setEnabled(enabled: Boolean) {
        equalizerManager.setEnabled(enabled)
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        equalizerManager.setBandLevel(bandIndex, level)
    }

    fun applyPreset(levels: List<Int>) {
        equalizerManager.applyPreset(levels)
    }

    // ---------- Bass Boost ----------

    fun setBassBoostEnabled(enabled: Boolean) {
        equalizerManager.setBassBoostEnabled(enabled)
    }

    fun setBassBoostStrength(strength: Int) {
        // Optimistic UI update with debounced hardware write
        _uiState.value = _uiState.value.copy(bassBoostStrength = strength)
        bbStrengthJob?.cancel()
        bbStrengthJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            equalizerManager.setBassBoostStrength(strength)
        }
    }

    // ---------- Virtualizer ----------

    fun setVirtualizerEnabled(enabled: Boolean) {
        equalizerManager.setVirtualizerEnabled(enabled)
    }

    fun setVirtualizerStrength(strength: Int) {
        _uiState.value = _uiState.value.copy(virtualizerStrength = strength)
        virtStrengthJob?.cancel()
        virtStrengthJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            equalizerManager.setVirtualizerStrength(strength)
        }
    }

    // ---------- Loudness Enhancer ----------

    fun setLoudnessEnabled(enabled: Boolean) {
        equalizerManager.setLoudnessEnabled(enabled)
    }

    fun setLoudnessStrength(strength: Int) {
        _uiState.value = _uiState.value.copy(loudnessStrength = strength)
        loudnessStrengthJob?.cancel()
        loudnessStrengthJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            equalizerManager.setLoudnessStrength(strength)
        }
    }

    override fun onCleared() {
        bandDebounceJob?.cancel()
        bbStrengthJob?.cancel()
        virtStrengthJob?.cancel()
        loudnessStrengthJob?.cancel()
        super.onCleared()
    }
}
