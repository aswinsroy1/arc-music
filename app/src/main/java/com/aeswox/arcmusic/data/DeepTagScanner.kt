package com.aeswox.arcmusic.data

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

/**
 * Result of a deep file tag scan.
 *
 * @param isExplicit      True when an explicit-content marker was found in the file tags.
 * @param isDolbyAtmos    True when the file codec is Dolby EAC-3/AC-3 (Dolby Atmos/Digital).
 * @param codec           Raw MIME type string from MediaExtractor (e.g. "audio/mp4a-latm").
 * @param bitDepth        Bit depth extracted from MediaExtractor pcm-encoding or bits-per-sample.
 * @param sampleRate      Sample rate in Hz (from MediaExtractor, more accurate than JAudioTagger).
 * @param channelCount    Number of audio channels.
 * @param year            4-digit year from embedded tag (more precise than MediaStore YEAR column).
 * @param composer        Composer field from embedded tag.
 * @param trackNumber     Track number from embedded tag.
 * @param discNumber      Disc number from embedded tag.
 * @param bitrate         Bitrate in bps from jaudiotagger audio header.
 * @param hasLyrics       True when embedded lyrics were found (any non-blank LYRICS field).
 */
data class DeepScanResult(
    val isExplicit: Boolean,
    val isDolbyAtmos: Boolean,
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

private const val DEEP_TAG = "ARC_DEEP_SCAN"

object DeepTagScanner {

    /**
     * Dolby Atmos / Dolby Digital codec MIME types reported by MediaExtractor.
     * EAC-3 (Enhanced AC-3) is the primary Dolby Atmos codec in AAC/EC-3 containers.
     */
    private val DOLBY_CODECS = setOf(
        "audio/eac3",   // EAC-3 / Dolby Atmos (primary)
        "audio/ec-3",   // Alternate representation
        "audio/ac3",    // Dolby Digital (AC-3)
        "audio/ac-3"    // Alternate representation
    )

    /**
     * Performs a deep metadata scan on the given audio file.
     *
     * Two phases:
     *  1. jaudiotagger — reads ID3/APE/MP4/Vorbis tags for explicit flag, lyrics,
     *     composer, year, track/disc numbers, bitrate from header.
     *  2. MediaExtractor — reads the raw codec, sample rate, channel count, and
     *     bit depth from the container. This is the only reliable source for
     *     Dolby Atmos detection and Hi-Res bit depth.
     *
     * The [File.exists] check is intentionally kept here because DeepTagScanner is
     * called on files that were already in the database — they need to exist on disk
     * for tag reading to work. (Unlike the MediaStore scanner, which should trust
     * ContentResolver URIs without checking the filesystem path.)
     */
    fun scanFile(filePath: String): DeepScanResult {
        var isExplicit       = false
        var isDolbyAtmos     = false
        var exactCodec: String? = null
        var exactBitDepth: Int? = null
        var exactSampleRate: Int? = null
        var exactChannelCount: Int? = null
        var exactYear: Int? = null
        var exactComposer: String? = null
        var exactTrackNumber: Int? = null
        var exactDiscNumber: Int? = null
        var exactBitrate: Int? = null
        var hasLyrics        = false

        val file = File(filePath)
        if (!file.exists()) {
            Log.d(DEEP_TAG, "SKIP (file not found): $filePath")
            return DeepScanResult(
                isExplicit = false, isDolbyAtmos = false, codec = null,
                bitDepth = null, sampleRate = null, channelCount = null,
                year = null, composer = null, trackNumber = null, discNumber = null,
                bitrate = null, hasLyrics = false
            )
        }

        // ── Phase 1: jaudiotagger tag reading ─────────────────────────────────
        try {
            val audioFile = AudioFileIO.read(file)

            // Bitrate from audio header (more accurate than MediaStore BITRATE column)
            audioFile.audioHeader?.let { header ->
                val kbps = header.bitRateAsNumber
                if (kbps > 0) exactBitrate = (kbps * 1000).toInt()
            }

            audioFile.tag?.let { tag ->
                // Year — prefer the tag YEAR field; fall back to ORIGINAL_YEAR
                exactYear = tag.getFirst(FieldKey.YEAR)
                    .takeIf { it.isNotBlank() }?.filter { it.isDigit() }?.take(4)?.toIntOrNull()
                    ?: tag.getFirst(FieldKey.ORIGINAL_YEAR)
                        .takeIf { it.isNotBlank() }?.filter { it.isDigit() }?.take(4)?.toIntOrNull()

                exactComposer = tag.getFirst(FieldKey.COMPOSER).takeIf { it.isNotBlank() }

                val trackStr = tag.getFirst(FieldKey.TRACK)
                if (trackStr.isNotBlank()) {
                    exactTrackNumber = trackStr.substringBefore("/").toIntOrNull()
                }

                val discStr = tag.getFirst(FieldKey.DISC_NO)
                if (discStr.isNotBlank()) {
                    exactDiscNumber = discStr.substringBefore("/").toIntOrNull()
                }

                // Embedded lyrics detection
                // Works for: MP3 (USLT), FLAC (LYRICS), M4A (©lyr), OGG (LYRICS)
                val embeddedLyrics = tag.getFirst(FieldKey.LYRICS)
                if (embeddedLyrics.isNotBlank()) {
                    hasLyrics = true
                    Log.d(DEEP_TAG, "Embedded lyrics found in: ${file.name}")
                }

                // ── Explicit detection ─────────────────────────────────────────
                // Strategy 1: scan all tag fields for known explicit markers
                val fieldIter = tag.fields
                while (fieldIter.hasNext()) {
                    val field = fieldIter.next()
                    val fieldId    = field.id.uppercase()
                    val fieldValue = field.toString().trim()

                    // iTunes Rating (RTNG) / iTunesAdvisory: value 1 = explicit
                    if (fieldId == "RTNG" || fieldId == "ITUNESADVISORY" || fieldId.contains("EXPLICIT")) {
                        if (fieldValue == "1" || fieldValue.equals("explicit", ignoreCase = true)) {
                            isExplicit = true
                            Log.d(DEEP_TAG, "Explicit via field [$fieldId=$fieldValue]: ${file.name}")
                            break
                        }
                    }
                    // ID3v2 TXXX:ITUNESADVISORY custom frame
                    if (fieldId.startsWith("TXXX")) {
                        val upper = fieldValue.uppercase()
                        if (upper.contains("ITUNESADVISORY") && (upper.contains("=1") || upper.endsWith("1"))) {
                            isExplicit = true
                            Log.d(DEEP_TAG, "Explicit via TXXX frame: ${file.name}")
                            break
                        }
                    }
                }

                // Strategy 2: try FieldKey.RATING which maps to the same frame in some formats
                if (!isExplicit) {
                    try {
                        val rating = tag.getFirst(FieldKey.RATING)
                        if (rating == "1" || rating.equals("explicit", ignoreCase = true)) {
                            isExplicit = true
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.w(DEEP_TAG, "jaudiotagger failed for ${file.name}: ${e.message}")
        }

        // ── Phase 2: MediaExtractor — codec + bit depth ───────────────────────
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(filePath)
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime   = format.getString(MediaFormat.KEY_MIME) ?: continue

                if (mime.startsWith("audio/")) {
                    exactCodec = mime

                    // Dolby Atmos / Digital detection
                    if (mime.lowercase() in DOLBY_CODECS) {
                        isDolbyAtmos = true
                        Log.d(DEEP_TAG, "Dolby Atmos/Digital detected ($mime): ${file.name}")
                    }

                    if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        exactSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }
                    if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        exactChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }

                    // Bit depth — two possible keys depending on Android version / codec
                    if (format.containsKey("pcm-encoding")) {
                        exactBitDepth = when (format.getInteger("pcm-encoding")) {
                            2  -> 16  // ENCODING_PCM_16BIT
                            21 -> 24  // ENCODING_PCM_24BIT_PACKED (API 31+)
                            22 -> 32  // ENCODING_PCM_32BIT (API 31+)
                            4  -> 32  // ENCODING_PCM_FLOAT
                            else -> null
                        }
                    } else if (format.containsKey("bits-per-sample")) {
                        exactBitDepth = format.getInteger("bits-per-sample")
                    }

                    break // Only inspect first audio track
                }
            }
        } catch (e: Exception) {
            Log.w(DEEP_TAG, "MediaExtractor failed for ${file.name}: ${e.message}")
        } finally {
            extractor.release()
        }

        return DeepScanResult(
            isExplicit    = isExplicit,
            isDolbyAtmos  = isDolbyAtmos,
            codec         = exactCodec,
            bitDepth      = exactBitDepth,
            sampleRate    = exactSampleRate,
            channelCount  = exactChannelCount,
            year          = exactYear,
            composer      = exactComposer,
            trackNumber   = exactTrackNumber,
            discNumber    = exactDiscNumber,
            bitrate       = exactBitrate,
            hasLyrics     = hasLyrics
        )
    }
}
