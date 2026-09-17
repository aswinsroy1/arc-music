package com.aeswox.arcmusic.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aeswox.arcmusic.R
import com.aeswox.arcmusic.data.SettingsRepository
import com.aeswox.arcmusic.db.MediaScannerManager
import com.aeswox.arcmusic.db.MusicRepository
import com.aeswox.arcmusic.db.ScanPhase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import com.aeswox.arcmusic.data.ScanLogger
import javax.inject.Inject

private const val STAG = "ARC_SCAN_DIAG"

@AndroidEntryPoint
class MediaScannerService : Service() {

    @Inject
    lateinit var repository: MusicRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var mediaScannerManager: MediaScannerManager

    @Inject
    lateinit var scanLogger: ScanLogger

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        const val ACTION_SCAN = "com.aeswox.arcmusic.action.SCAN"
        const val ACTION_SCAN_TARGET = "com.aeswox.arcmusic.action.SCAN_TARGET"
        const val ACTION_INCREMENTAL_SCAN = "com.aeswox.arcmusic.action.INCREMENTAL_SCAN"
        const val ACTION_REBUILD = "com.aeswox.arcmusic.action.REBUILD"
        const val EXTRA_TARGET_FOLDER = "com.aeswox.arcmusic.extra.TARGET_FOLDER"
        
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "media_scanner_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        val notification = createNotification("Scanning Media Library...", 0, 0)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        when (action) {
            ACTION_SCAN -> performScan(null, incrementalOnly = false)
            ACTION_INCREMENTAL_SCAN -> performScan(null, incrementalOnly = true)
            ACTION_SCAN_TARGET -> performScan(intent.getStringExtra(EXTRA_TARGET_FOLDER), incrementalOnly = false)
            ACTION_REBUILD -> performRebuild()
        }

        return START_NOT_STICKY
    }

    private fun performScan(targetFolder: String? = null, incrementalOnly: Boolean = false) {
        if (mediaScannerManager.scanProgress.value.isRunning) {
            scanLogger.w("performScan() SKIPPED: isRunning=true (scan already in progress or stuck).")
            scanLogger.w("  If you just pressed Scan and see this, the previous scan is still running")
            scanLogger.w("  OR the isRunning flag got stuck. Force-stop the app to reset.")
            return
        }
        scanLogger.i("performScan() STARTED targetFolder=$targetFolder incrementalOnly=$incrementalOnly")
        
        serviceScope.launch {
            // Defer scan if playback is active (user preference)
            val shouldDefer = settingsRepository.deferScanDuringPlayback.first()
            if (shouldDefer && PlaybackService.isCurrentlyPlaying) {
                scanLogger.i("performScan() DEFERRED: playback is active, waiting 30s...")
                kotlinx.coroutines.delay(30_000L)
            }

            mediaScannerManager.updateProgress(isRunning = true, phase = ScanPhase.FETCHING_MEDIASTORE)
            try {
                val minDurMs       = settingsRepository.minSongDurationSec.first() * 1000L
                val minTracks      = settingsRepository.minTracksPerAlbum.first()
                val excluded       = settingsRepository.excludedFolders.first()
                val minBitrateKbps = settingsRepository.minBitrateKbps.first()
                val augmentMeta    = settingsRepository.augmentMetadataFromTags.first()
                val sinceTimestamp = if (incrementalOnly) {
                    // Convert epoch-ms to epoch-seconds for MediaStore DATE_MODIFIED comparison
                    settingsRepository.lastSyncTimestamp.first() / 1000L
                } else 0L

                scanLogger.i("performScan() sinceTimestampSec=$sinceTimestamp minBitrateKbps=$minBitrateKbps augment=$augmentMeta")

                val result = repository.scanMediaStore(
                    minDurationMs      = minDurMs,
                    minTracksPerAlbum  = minTracks,
                    excludedFolders    = excluded,
                    targetFolder       = targetFolder,
                    sinceTimestampSec  = sinceTimestamp,
                    minBitrateKbps     = minBitrateKbps,
                    augmentMetadata    = augmentMeta
                ) { phase, current, total ->
                    mediaScannerManager.updateProgress(
                        isRunning = true,
                        phase = phase,
                        current = current,
                        total = total
                    )
                    updateNotificationProgress(phase, current, total)
                }

                // Save the scan timestamp so future INCREMENTAL scans skip unchanged files
                settingsRepository.setLastSyncTimestamp(System.currentTimeMillis())

                mediaScannerManager.updateResult(result)
                mediaScannerManager.updateProgress(
                    isRunning = false,
                    phase = ScanPhase.COMPLETING,
                    current = result.trackCount,
                    total = result.trackCount,
                    isCompleted = true
                )
                scanLogger.i("performScan() COMPLETED: tracks=${result.trackCount} albums=${result.albumCount} artists=${result.artistCount}")
                scanLogger.i("  If the new file is NOT reflected, check DROPPED lines in scanner log above.")
            } catch (e: Exception) {
                mediaScannerManager.updateResult(null)
                mediaScannerManager.updateProgress(isRunning = false, isCompleted = false)
                scanLogger.e("performScan() FAILED: ${e.message}")
            } finally {
                startService(Intent(this@MediaScannerService, MetadataEnrichmentService::class.java))
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun performRebuild() {
        if (mediaScannerManager.scanProgress.value.isRunning) return

        serviceScope.launch {
            mediaScannerManager.updateProgress(isRunning = true, phase = ScanPhase.CLEARING_DATABASE)
            try {
                val minDurMs       = settingsRepository.minSongDurationSec.first() * 1000L
                val minTracks      = settingsRepository.minTracksPerAlbum.first()
                val excluded       = settingsRepository.excludedFolders.first()
                val minBitrateKbps = settingsRepository.minBitrateKbps.first()

                val result = repository.rebuildDatabase(
                    minDurationMs      = minDurMs,
                    minTracksPerAlbum  = minTracks,
                    excludedFolders    = excluded,
                    minBitrateKbps     = minBitrateKbps
                ) { phase, current, total ->
                    mediaScannerManager.updateProgress(
                        isRunning = true,
                        phase = phase,
                        current = current,
                        total = total
                    )
                    updateNotificationProgress(phase, current, total)
                }

                // Reset the sync timestamp so the next scan starts fully fresh
                settingsRepository.setLastSyncTimestamp(System.currentTimeMillis())

                mediaScannerManager.updateResult(result)
                mediaScannerManager.updateProgress(
                    isRunning = false,
                    phase = ScanPhase.COMPLETING,
                    current = result.trackCount,
                    total = result.trackCount,
                    isCompleted = true
                )
            } catch (e: Exception) {
                mediaScannerManager.updateResult(null)
                mediaScannerManager.updateProgress(isRunning = false, isCompleted = false)
            } finally {
                startService(Intent(this@MediaScannerService, MetadataEnrichmentService::class.java))
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun updateNotificationProgress(phase: ScanPhase, current: Int, total: Int) {
        val title = when (phase) {
            ScanPhase.FETCHING_MEDIASTORE -> "Fetching media..."
            ScanPhase.PROCESSING_FILES -> "Processing files..."
            ScanPhase.CLEARING_DATABASE -> "Clearing database..."
            ScanPhase.COMPLETING -> "Finishing up..."
            else -> "Scanning..."
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(title, current, total))
    }

    private fun createNotification(title: String, current: Int, total: Int): Notification {
        val progressText = if (total > 0) "$current / $total" else ""
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(progressText)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(if (total > 0) total else 0, current, total == 0)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Scanner",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of library scanning"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
