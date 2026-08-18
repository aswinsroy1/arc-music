package com.aeswox.arcmusic.db

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ScanProgress(
    val isRunning: Boolean = false,
    val phase: ScanPhase = ScanPhase.FETCHING_MEDIASTORE,
    val current: Int = 0,
    val total: Int = 0,
    val isCompleted: Boolean = false
) {
    val fraction: Float get() = if (total > 0) current.toFloat() / total else 0f
    val hasProgress: Boolean get() = total > 0
}

@Singleton
class MediaScannerManager @Inject constructor() {
    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    fun updateProgress(
        isRunning: Boolean = _scanProgress.value.isRunning,
        phase: ScanPhase = _scanProgress.value.phase,
        current: Int = _scanProgress.value.current,
        total: Int = _scanProgress.value.total,
        isCompleted: Boolean = _scanProgress.value.isCompleted
    ) {
        _scanProgress.value = ScanProgress(
            isRunning = isRunning,
            phase = phase,
            current = current,
            total = total,
            isCompleted = isCompleted
        )
    }

    fun clearProgress() {
        _scanProgress.value = ScanProgress()
    }

    fun updateResult(result: ScanResult?) {
        _scanResult.value = result
    }
}
