package com.aeswox.arcmusic.data

import android.media.MediaExtractor
import android.media.MediaFormat
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

data class DeepScanResult(
    val isExplicit: Boolean,
    val codec: String?,
    val bitDepth: Int?,
    val sampleRate: Int?,
    val channelCount: Int?,
    val year: Int?,
    val composer: String?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val bitrate: Int?,
    val hasLyrics: Boolean
)

object DeepTagScanner {

    fun scanFile(filePath: String): DeepScanResult {
        var isExplicit = false
        var exactCodec: String? = null
        var exactBitDepth: Int? = null
        var exactSampleRate: Int? = null
        var exactChannelCount: Int? = null
        var exactYear: Int? = null
        var exactComposer: String? = null
        var exactTrackNumber: Int? = null
        var exactDiscNumber: Int? = null
        var exactBitrate: Int? = null
        var hasLyrics = false

        val file = File(filePath)
        if (!file.exists()) return DeepScanResult(false, null, null, null, null, null, null, null, null, null, false)

        // 1. Check for Explicit Tag via jaudiotagger
        try {
            val audioFile = AudioFileIO.read(file)
            
            val header = audioFile.audioHeader
            if (header != null) {
                val kbps = header.bitRateAsNumber
                if (kbps > 0) {
                    exactBitrate = (kbps * 1000).toInt()
                }
            }
            
            val tag = audioFile.tag
            if (tag != null) {
                exactYear = tag.getFirst(FieldKey.YEAR).takeIf { it.isNotBlank() }?.filter { it.isDigit() }?.take(4)?.toIntOrNull()
                    ?: tag.getFirst(FieldKey.ORIGINAL_YEAR).takeIf { it.isNotBlank() }?.filter { it.isDigit() }?.take(4)?.toIntOrNull()
                    
                exactComposer = tag.getFirst(FieldKey.COMPOSER).takeIf { it.isNotBlank() }
                
                val trackStr = tag.getFirst(FieldKey.TRACK)
                if (trackStr.isNotBlank()) {
                    exactTrackNumber = trackStr.substringBefore("/").toIntOrNull()
                }
                
                val discStr = tag.getFirst(FieldKey.DISC_NO)
                if (discStr.isNotBlank()) {
                    exactDiscNumber = discStr.substringBefore("/").toIntOrNull()
                }
                
                val lyrics = tag.getFirst(FieldKey.LYRICS)
                if (lyrics.isNotBlank()) {
                    hasLyrics = true
                }
                
                // Manually search fields for explicit markers
                val fields = tag.fields
                while (fields.hasNext()) {
                    val field = fields.next()
                    val id = field.id.uppercase()
                    val value = field.toString().trim()
                    
                    if (id == "RTNG" || id == "ITUNESADVISORY" || id.contains("EXPLICIT")) {
                        if (value == "1" || value.equals("Explicit", ignoreCase = true)) {
                            isExplicit = true
                            break
                        }
                    }
                    // ID3v2 TXXX frames often look like "TXXX:ITUNESADVISORY"
                    if (id.startsWith("TXXX")) {
                        if (value.contains("ITUNESADVISORY", ignoreCase = true) && value.contains("1")) {
                            isExplicit = true
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Deep inspect codec via MediaExtractor (for Dolby Atmos and exact Hi-Res info)
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(filePath)
            if (extractor.trackCount > 0) {
                // Usually audio is the first track for music files
                for (i in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                    
                    if (mime.startsWith("audio/")) {
                        exactCodec = mime
                        
                        if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                            exactSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        }
                        if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                            exactChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        }
                        // Android MediaFormat introduced KEY_PCM_ENCODING for bit depths in some versions
                        if (format.containsKey("pcm-encoding")) {
                            val encoding = format.getInteger("pcm-encoding")
                            // android.media.AudioFormat.ENCODING_PCM_16BIT = 2
                            // android.media.AudioFormat.ENCODING_PCM_24BIT_PACKED = 21 (API 31)
                            // android.media.AudioFormat.ENCODING_PCM_32BIT = 22 (API 31)
                            // android.media.AudioFormat.ENCODING_PCM_FLOAT = 4
                            when (encoding) {
                                2 -> exactBitDepth = 16
                                21 -> exactBitDepth = 24
                                22 -> exactBitDepth = 32
                                4 -> exactBitDepth = 32 // Float is usually 32-bit
                            }
                        } else if (format.containsKey("bits-per-sample")) {
                             exactBitDepth = format.getInteger("bits-per-sample")
                        }
                        break // We only care about the first audio track
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
        }

        return DeepScanResult(
            isExplicit = isExplicit,
            codec = exactCodec,
            bitDepth = exactBitDepth,
            sampleRate = exactSampleRate,
            channelCount = exactChannelCount,
            year = exactYear,
            composer = exactComposer,
            trackNumber = exactTrackNumber,
            discNumber = exactDiscNumber,
            bitrate = exactBitrate,
            hasLyrics = hasLyrics
        )
    }
}
