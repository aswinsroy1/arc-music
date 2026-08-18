package com.aeswox.arcmusic

import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExtractorTest {
    @Test
    fun testExtractor() {
        val file = File("/sdcard/Music/Spotiflac/stay - Ariana Grande.m4a")
        
        Log.d("EAC3_TEST", "Testing " + file.name)
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(file.absolutePath)
            var foundAudio = false
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    val duration = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else -1L
                    val bitrate = if (format.containsKey(MediaFormat.KEY_BIT_RATE)) format.getInteger(MediaFormat.KEY_BIT_RATE) else -1
                    Log.d("EAC3_TEST", "  MediaExtractor: MIME: " + mime + ", Duration: " + duration + ", Bitrate: " + bitrate)
                    foundAudio = true
                    break
                }
            }
            if (!foundAudio) {
                Log.d("EAC3_TEST", "  MediaExtractor: No audio track found")
            }
        } catch (e: Exception) {
            Log.e("EAC3_TEST", "  MediaExtractor: Exception", e)
        } finally {
            extractor.release()
        }
        
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            Log.d("EAC3_TEST", "  MediaMetadataRetriever: Duration: " + durationMs + ", Bitrate: " + bitrate)
        } catch (e: Exception) {
            Log.e("EAC3_TEST", "  MediaMetadataRetriever: Exception", e)
        } finally {
            retriever.release()
        }
    }
}
