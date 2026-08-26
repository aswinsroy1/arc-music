package com.aeswox.arcmusic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheWriter
import com.aeswox.arcmusic.MainActivity
import com.aeswox.arcmusic.db.MusicRepository
import com.aeswox.arcmusic.network.AppleMusicCanvasProvider
import com.aeswox.arcmusic.network.CanvasCacheManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class CanvasFetchService : Service() {

    @Inject lateinit var musicRepository: MusicRepository
    @Inject lateinit var canvasProvider: AppleMusicCanvasProvider
    @Inject lateinit var canvasCacheManager: CanvasCacheManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val NOTIFICATION_ID = 8484
    private val CHANNEL_ID = "canvas_fetch_channel"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Starting fetch..."))

        serviceScope.launch {
            try {
                processCanvases()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopForeground(true)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    @OptIn(UnstableApi::class)
    private suspend fun processCanvases() {
        val tracks = musicRepository.getAllTracks().first()
        val total = tracks.size
        var current = 0

        for (track in tracks) {
            current++
            updateNotification("Fetching $current / $total: ${track.title}")
            
            if (track.canvasUrl != null) continue

            try {
                val url = canvasProvider.getCanvasUrl(track.title, track.artist, track.album)
                if (url != null) {
                    musicRepository.updateCanvasUrl(track.id, url, System.currentTimeMillis())
                    
                    val dataSpec = DataSpec(android.net.Uri.parse(url))
                    val factory = canvasCacheManager.getCacheDataSourceFactory()
                    
                    val cacheWriter = CacheWriter(
                        factory.createDataSource(),
                        dataSpec,
                        null,
                        null
                    )
                    
                    try {
                        cacheWriter.cache()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    musicRepository.updateCanvasUrl(track.id, null, System.currentTimeMillis())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            delay(2000)
        }
    }

    private fun createNotification(text: String): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fetching Canvases")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canvas Fetch Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of background canvas downloading"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
