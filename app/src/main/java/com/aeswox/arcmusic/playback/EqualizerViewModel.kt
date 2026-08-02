package com.aeswox.arcmusic.playback

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerManager: EqualizerManager
) : ViewModel() {
    
    val isEnabled = equalizerManager.isEnabled
    val bandLevels = equalizerManager.bandLevels

    fun setEnabled(enabled: Boolean) {
        equalizerManager.setEnabled(enabled)
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        equalizerManager.setBandLevel(bandIndex, level)
    }
    fun applyPreset(levels: List<Int>) {
        equalizerManager.applyPreset(levels)
    }
}
