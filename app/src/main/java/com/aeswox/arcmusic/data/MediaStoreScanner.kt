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
    fun scanAudioFiles(targetFolder: String? = null): List<ScannedTrack> {
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

        var selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val selectionArgs = mutableListOf<String>()
        if (targetFolder != null) {
            selection += " AND ${MediaStore.Audio.Media.DATA} LIKE ?"
            selectionArgs.add("$targetFolder%")
        }

        context.contentResolver.query(
            uri,
            projection,
            selection,
            if (selectionArgs.isEmpty()) null else selectionArgs.toTypedArray(),
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

            val seenPaths = mutableSetOf<String>()
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
                
                val yearColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.YEAR)
                val year = if (yearColumn >= 0) cursor.getInt(yearColumn) else null
                
                // Track number might include disc number in some formats, e.g., 1004 for disc 1 track 4
                val fullTrackNumber = cursor.getInt(trackColumn)
                val trackNumber = fullTrackNumber % 1000
                val discNumber = if (fullTrackNumber >= 1000) fullTrackNumber / 1000 else 1
                
                var durationMs = cursor.getLong(durationColumn)
                val filePath = cursor.getString(dataColumn) ?: ""
                
                if (filePath.isBlank() || !java.io.File(filePath).exists() || !seenPaths.add(filePath)) {
                    continue
                }
                
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

                if (durationMs == 0L) {
                    try {
                        val retriever = android.media.MediaMetadataRetriever()
                        retriever.setDataSource(filePath)
                        val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                        if (durStr != null) {
                            durationMs = durStr.toLong()
                        }
                        retriever.release()
                    } catch (e: Exception) {
                        android.util.Log.e("MediaStoreScanner", "Retriever failed for $filePath", e)
                    }
                    if (durationMs == 0L && (filePath.endsWith(".mp4", true) || filePath.endsWith(".m4a", true))) {
                        durationMs = extractMp4DurationMs(filePath)
                    }
                }
                
                var estimatedBitrate = bitrate
                if (estimatedBitrate <= 0 && durationMs > 0) {
                    val durationSecs = durationMs / 1000.0
                    if (durationSecs > 0) {
                        estimatedBitrate = ((sizeBytes * 8) / durationSecs).toInt()
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
                        year = year,
                        trackNumber = trackNumber,
                        discNumber = discNumber,
                        durationMs = durationMs,
                        filePath = filePath,
                        fileSizeBytes = sizeBytes,
                        mimeType = actualMimeType,
                        bitrate = estimatedBitrate,
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
            android.util.Log.e("MediaStoreScanner", "Custom parser failed for $filePath", e)
        }
        return 0L
    }

    fun getFoldersContainingAudio(): List<String> {
        val folders = mutableSetOf<String>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumn) ?: ""
                val folder = filePath.substringBeforeLast('/')
                if (folder.isNotBlank() && folder.startsWith("/")) {
                    folders.add(folder)
                }
            }
        }
        return folders.toList().sorted()
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
    val year: Int?,
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
