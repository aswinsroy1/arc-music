package com.aeswox.arcmusic.data

import android.content.Context
import android.media.AudioFormat
import android.media.MediaExtractor
import android.media.MediaFormat
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scanAudioFiles(): List<ScannedTrack> {
        val tracks = mutableListOf<ScannedTrack>()
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // File path
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.IS_MUSIC
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val albumArtistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
            val genreColumn = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val bitrateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn).toString()
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val albumId = if (albumIdColumn >= 0) cursor.getLong(albumIdColumn) else null
                
                val albumArtist = if (albumArtistColumn >= 0) {
                    cursor.getString(albumArtistColumn) ?: artist
                } else artist
                
                val genre = if (genreColumn >= 0) {
                    cursor.getString(genreColumn) ?: ""
                } else ""
                
                // Track number might include disc number in some formats, e.g., 1004 for disc 1 track 4
                val fullTrackNumber = cursor.getInt(trackColumn)
                val trackNumber = fullTrackNumber % 1000
                val discNumber = if (fullTrackNumber >= 1000) fullTrackNumber / 1000 else 1
                
                var durationMs = cursor.getLong(durationColumn)
                val filePath = cursor.getString(dataColumn) ?: ""
                val sizeBytes = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn) * 1000L // MediaStore stores in seconds usually, wait, DATE_ADDED is in seconds!
                val dateModified = cursor.getLong(dateModifiedColumn) * 1000L
                val mimeType = cursor.getString(mimeTypeColumn) ?: ""
                val bitrate = if (bitrateColumn >= 0) cursor.getInt(bitrateColumn) else 0

                var actualMimeType = mimeType
                var sampleRate = 0
                var bitDepth = 0

                val lowerPath = filePath.lowercase()
                // Determine if we need a deep scan for certain formats where MediaStore may miss metadata
                val needsDeepScan = lowerPath.endsWith(".flac") || lowerPath.endsWith(".wav") || 
                                    lowerPath.endsWith(".alac") || lowerPath.endsWith(".m4a") || 
                                    lowerPath.endsWith(".eac3") || lowerPath.endsWith(".ac3")

                if (needsDeepScan) {
                    val extractor = MediaExtractor()
                    try {
                        extractor.setDataSource(filePath)
                        for (i in 0 until extractor.trackCount) {
                            val format = extractor.getTrackFormat(i)
                            val trackMime = format.getString(MediaFormat.KEY_MIME) ?: continue
                            if (trackMime.startsWith("audio/")) {
                                actualMimeType = trackMime

                                // Sample rate extraction
                                if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                                    sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                                }

                                // Bit depth extraction
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
                                        else -> 0
                                    }
                                }

                                // If MediaStore gave a zero duration, fall back to extractor's duration (microseconds)
                                if (durationMs == 0L && format.containsKey(MediaFormat.KEY_DURATION)) {
                                    durationMs = format.getLong(MediaFormat.KEY_DURATION) / 1000L
                                }

                                break
                            }
                        }
                    } catch (e: Exception) {
                        // Silently ignore any extraction issues – we still retain any previously read data
                    } finally {
                        extractor.release()
                    }
                }

                val artworkUri = if (albumId != null) {
                    val uriStr = "content://media/external/audio/albumart/$albumId"
                    val uri = android.net.Uri.parse(uriStr)
                    var exists = false
                    try {
                        context.contentResolver.openInputStream(uri)?.use { exists = true }
                    } catch (e: Exception) {}
                    if (exists) uriStr else null
                } else null

                tracks.add(
                    ScannedTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        albumArtist = albumArtist,
                        albumId = albumId,
                        album = album,
                        genre = genre,
                        trackNumber = trackNumber,
                        discNumber = discNumber,
                        durationMs = durationMs,
                        filePath = filePath,
                        fileSizeBytes = sizeBytes,
                        mimeType = actualMimeType,
                        bitrate = bitrate,
                        sampleRate = sampleRate,
                        bitDepth = bitDepth,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        artworkUri = artworkUri
                    )
                )
            }
        }
        return tracks
    }
}

data class ScannedTrack(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtist: String,
    val albumId: Long?,
    val album: String,
    val genre: String,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long,
    val filePath: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val bitrate: Int,
    val sampleRate: Int,
    val bitDepth: Int,
    val dateAdded: Long,
    val dateModified: Long,
    val artworkUri: String?
)
