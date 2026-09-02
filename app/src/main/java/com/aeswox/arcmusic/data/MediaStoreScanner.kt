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

                val artworkUriStr = if (albumId != null) "content://media/external/audio/albumart/$albumId" else null
                
                var estimatedBitrate = bitrate
                if (estimatedBitrate <= 0 && durationMs > 0) {
                    val durationSecs = durationMs / 1000.0
                    if (durationSecs > 0) {
                        estimatedBitrate = ((sizeBytes * 8) / durationSecs).toInt()
                    }
                }

                val hasExplicitTag = title.contains("[E]", ignoreCase = true) || 
                                     title.contains("(Explicit)", ignoreCase = true) || 
                                     filePath.contains("[E]", ignoreCase = true) || 
                                     filePath.contains("(Explicit)", ignoreCase = true)

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
                        mimeType = mimeType,
                        bitrate = estimatedBitrate,
                        sampleRate = null,
                        bitDepth = null,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        artworkUri = artworkUriStr,
                        isExplicit = if (hasExplicitTag) true else null
                    )
                )
            }
        }

        return tracks
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
    val sampleRate: Int?,
    val bitDepth: Int?,
    val dateAdded: Long,
    val dateModified: Long,
    val artworkUri: String?,
    val isExplicit: Boolean? = null
)
