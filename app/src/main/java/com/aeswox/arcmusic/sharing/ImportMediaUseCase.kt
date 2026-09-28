package com.aeswox.arcmusic.sharing

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

import com.aeswox.arcmusic.db.MusicRepository
import com.aeswox.arcmusic.db.MediaScannerManager
import kotlinx.coroutines.flow.first

class ImportMediaUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
    private val mediaScannerManager: MediaScannerManager
) {
    private var receivedPlaylistFile: File? = null
    private var receivedPlaylistName: String? = null
    suspend fun processReceivedPayload(receivedUri: android.net.Uri?, receivedFile: File?, metadata: JSONObject) = withContext(Dispatchers.IO) {
        try {
            val type = metadata.getString("type")
            val resolver = context.contentResolver
            
            fun getInputStream() = receivedUri?.let { resolver.openInputStream(it) } ?: receivedFile?.inputStream()

            if (type == "track") {
                val title = metadata.optString("title", "Unknown Track")
                val ext = metadata.optString("ext", "flac")
                var filename = metadata.optString("filename", "$title.$ext")
                
                val relativePath = Environment.DIRECTORY_MUSIC + "/ArcMusic/"
                val destDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "ArcMusic")
                
                var isDuplicate = false
                val projection = arrayOf(MediaStore.Audio.Media._ID)
                val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ? AND ${MediaStore.Audio.Media.RELATIVE_PATH} = ?"
                
                resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, arrayOf(filename, relativePath), null)?.use { cursor ->
                    if (cursor.count > 0) isDuplicate = true
                }
                if (File(destDir, filename).exists()) {
                    isDuplicate = true
                }

                if (isDuplicate) {
                    val nameWithoutExt = filename.substringBeforeLast(".")
                    filename = "$nameWithoutExt (received).$ext"
                    var counter = 1
                    while (File(destDir, filename).exists()) {
                        filename = "$nameWithoutExt (received $counter).$ext"
                        counter++
                    }
                    Log.i("ImportMediaUseCase", "Duplicate filename detected, renamed to $filename")
                }
                
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/*")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        getInputStream()?.use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    values.clear()
                    values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            } else if (type == "playlist_m3u") {
                val playlistName = metadata.optString("playlistName", "Playlist_${System.currentTimeMillis()}")
                val sanitizedName = playlistName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                
                val destDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "ArcMusic")
                if (!destDir.exists()) destDir.mkdirs()
                
                val m3uFile = File(destDir, "$sanitizedName.m3u")
                m3uFile.outputStream().use { outStream ->
                    getInputStream()?.use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
                receivedFile?.delete()
                
                receivedPlaylistFile = m3uFile
                receivedPlaylistName = playlistName
            }
        } catch (e: Exception) {
            Log.e("ImportMediaUseCase", "Failed to process received payload", e)
        }
    }

    suspend fun finalizeImport() {
        // Trigger quick incremental scan for new files
        val intent = Intent(context, com.aeswox.arcmusic.playback.MediaScannerService::class.java).apply {
            action = com.aeswox.arcmusic.playback.MediaScannerService.ACTION_INCREMENTAL_SCAN
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        // Wait for scanner to finish
        mediaScannerManager.scanProgress.first { !it.isRunning }

        // Now parse the M3U and create the Playlist using the newly scanned tracks!
        receivedPlaylistFile?.let { m3uFile ->
            try {
                val lines = m3uFile.readLines()
                val paths = lines.map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
                
                val allTracks = repository.getAllTracks().first()
                val trackIds = mutableListOf<String>()
                
                for (path in paths) {
                    val fileName = File(path).name
                    val matchedTrack = allTracks.find { it.filePath == path } 
                        ?: allTracks.find { it.filePath.endsWith(path) }
                        ?: allTracks.find { File(it.filePath).name == fileName }
                        
                    if (matchedTrack != null) {
                        trackIds.add(matchedTrack.id)
                    }
                }
                
                val finalName = receivedPlaylistName ?: "Imported Playlist"
                if (trackIds.isNotEmpty()) {
                    repository.createPlaylist(finalName, "Received via Nearby Share", null, trackIds)
                    Log.i("ImportMediaUseCase", "Successfully created playlist $finalName with ${trackIds.size} tracks")
                }
            } catch (e: Exception) {
                Log.e("ImportMediaUseCase", "Failed to parse imported M3U", e)
            }
            receivedPlaylistFile = null
            receivedPlaylistName = null
        }
    }
}
