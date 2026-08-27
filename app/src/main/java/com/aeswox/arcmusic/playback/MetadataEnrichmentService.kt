package com.aeswox.arcmusic.playback

import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.IBinder
import android.util.Log
import com.aeswox.arcmusic.db.MusicRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MetadataEnrichmentService : Service() {

    @Inject
    lateinit var repository: MusicRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            serviceScope.launch {
                try {
                    enrichMetadata()
                } catch (e: Exception) {
                    Log.e("MetadataEnrichment", "Error enriching metadata", e)
                } finally {
                    isRunning = false
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun enrichMetadata() {
        val tracksToEnrich = repository.getTracksMissingDeepMetadata()
        Log.d("MetadataEnrichment", "Found ${tracksToEnrich.size} tracks to enrich")

        for (track in tracksToEnrich) {
            val lowerPath = track.filePath.lowercase()
            val needsDeepScan = lowerPath.endsWith(".flac") || lowerPath.endsWith(".wav") || 
                                lowerPath.endsWith(".alac") || lowerPath.endsWith(".m4a") || 
                                lowerPath.endsWith(".eac3") || lowerPath.endsWith(".ac3")

            var durationMs = track.durationMs
            var sampleRate: Int? = null
            var bitDepth: Int? = null
            var codec: String? = null
            var estimatedBitrate = track.bitrate ?: 0

            if (needsDeepScan) {
                val extractor = MediaExtractor()
                try {
                    extractor.setDataSource(track.filePath)
                    for (i in 0 until extractor.trackCount) {
                        val format = extractor.getTrackFormat(i)
                        val trackMime = format.getString(MediaFormat.KEY_MIME) ?: continue
                        if (trackMime.startsWith("audio/")) {
                            codec = trackMime

                            if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            }

                            if (format.containsKey("bits-per-sample")) {
                                bitDepth = format.getInteger("bits-per-sample")
                            } else if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                                val encoding = format.getInteger(MediaFormat.KEY_PCM_ENCODING)
                                bitDepth = when (encoding) {
                                    AudioFormat.ENCODING_PCM_8BIT -> 8
                                    AudioFormat.ENCODING_PCM_16BIT -> 16
                                    AudioFormat.ENCODING_PCM_24BIT_PACKED,
                                    AudioFormat.ENCODING_PCM_FLOAT -> 24
                                    AudioFormat.ENCODING_PCM_32BIT -> 32
                                    else -> null
                                }
                            }

                            if (durationMs == 0L && format.containsKey(MediaFormat.KEY_DURATION)) {
                                durationMs = format.getLong(MediaFormat.KEY_DURATION) / 1000L
                            }
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Ignore extraction issues
                } finally {
                    extractor.release()
                }
            }

            if (durationMs == 0L) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(track.filePath)
                    val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    if (durStr != null) {
                        durationMs = durStr.toLong()
                    }
                    retriever.release()
                } catch (e: Exception) {
                    Log.e("MetadataEnrichment", "Retriever failed for ${track.filePath}", e)
                }
                if (durationMs == 0L && (track.filePath.endsWith(".mp4", true) || track.filePath.endsWith(".m4a", true))) {
                    durationMs = extractMp4DurationMs(track.filePath)
                }
            }

            if (estimatedBitrate <= 0 && durationMs > 0) {
                val durationSecs = durationMs / 1000.0
                if (durationSecs > 0) {
                    estimatedBitrate = ((track.fileSizeBytes * 8) / durationSecs).toInt()
                }
            }

            // Always update to clear the null values if we processed it, so we don't query it again endlessly.
            if (codec == null) {
                codec = track.codec ?: "unknown"
            }

            repository.updateTrackDeepMetadata(track.id, durationMs, sampleRate, bitDepth, estimatedBitrate, codec)
        }
        
        Log.d("MetadataEnrichment", "Enrichment complete")
    }
    
    private fun extractMp4DurationMs(filePath: String): Long {
        try {
            val file = java.io.RandomAccessFile(filePath, "r")
            var pos = 0L
            val length = file.length()
            var timescale = 0L
            var maxTfdt = 0L
            var mvhdDuration = 0L

            while (pos < length) {
                file.seek(pos)
                if (length - pos < 8) break
                var size = file.readInt().toLong() and 0xFFFFFFFFL
                val type = ByteArray(4)
                file.readFully(type)
                val typeStr = String(type)
                
                var headerLen = 8L
                if (size == 1L) {
                    if (length - pos < 16) break
                    size = file.readLong()
                    headerLen = 16L
                } else if (size == 0L) {
                    size = length - pos
                }
                if (size < headerLen) break

                when (typeStr) {
                    "moov", "trak", "mdia", "moof", "traf" -> {
                        pos += headerLen
                    }
                    "mvhd" -> {
                        val version = file.read()
                        file.read(ByteArray(3))
                        if (version == 1) {
                            file.readLong(); file.readLong()
                            val ts = file.readInt().toLong() and 0xFFFFFFFFL
                            val dur = file.readLong()
                            if (timescale == 0L) timescale = ts
                            if (dur > 0L) mvhdDuration = dur
                        } else {
                            file.readInt(); file.readInt()
                            val ts = file.readInt().toLong() and 0xFFFFFFFFL
                            val dur = file.readInt().toLong() and 0xFFFFFFFFL
                            if (timescale == 0L) timescale = ts
                            if (dur > 0L) mvhdDuration = dur
                        }
                        pos += size
                    }
                    "mdhd" -> {
                        val version = file.read()
                        file.read(ByteArray(3))
                        if (version == 1) {
                            file.readLong(); file.readLong()
                            val ts = file.readInt().toLong() and 0xFFFFFFFFL
                            val dur = file.readLong()
                            timescale = ts
                            if (mvhdDuration == 0L && dur > 0L) mvhdDuration = dur
                        } else {
                            file.readInt(); file.readInt()
                            val ts = file.readInt().toLong() and 0xFFFFFFFFL
                            val dur = file.readInt().toLong() and 0xFFFFFFFFL
                            timescale = ts
                            if (mvhdDuration == 0L && dur > 0L) mvhdDuration = dur
                        }
                        pos += size
                    }
                    "tfdt" -> {
                        val version = file.read()
                        file.read(ByteArray(3))
                        val baseDecodeTime = if (version == 1) file.readLong() else file.readInt().toLong() and 0xFFFFFFFFL
                        if (baseDecodeTime > maxTfdt) {
                            maxTfdt = baseDecodeTime
                        }
                        pos += size
                    }
                    else -> {
                        pos += size
                    }
                }
            }
            file.close()

            if (timescale > 0) {
                if (mvhdDuration > 0) {
                    return (mvhdDuration * 1000) / timescale
                } else if (maxTfdt > 0) {
                    return (maxTfdt * 1000) / timescale
                }
            }
        } catch (e: Exception) {
            Log.e("MetadataEnrichment", "Custom parser failed for $filePath", e)
        }
        return 0L
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
