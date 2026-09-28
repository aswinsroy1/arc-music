package com.aeswox.arcmusic.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.aeswox.arcmusic.db.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val nearbySharingManager: NearbySharingManager,
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentPayload: SharePayload? = null

    val sharingState = nearbySharingManager.sharingState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingState.IDLE
    )

    val discoveredEndpoints = nearbySharingManager.discoveredEndpoints.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val connectionRequest = nearbySharingManager.connectionRequest.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val transferProgress = nearbySharingManager.transferProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    init {
        val payloadType = savedStateHandle.get<String>("type")
        val payloadId = savedStateHandle.get<String>("id")
        
        if (payloadType != null && payloadId != null) {
            currentPayload = when (payloadType) {
                "track" -> SharePayload.SingleTrack(payloadId)
                "tracks" -> SharePayload.MultipleTracks(payloadId.split(","))
                "playlist" -> SharePayload.Playlist(payloadId)
                "artist" -> SharePayload.Artist(payloadId)
                "album" -> SharePayload.Album(payloadId)
                else -> null
            }
            currentPayload?.let { nearbySharingManager.setPayload(it) }
        }
    }

    fun startDiscovery() {
        nearbySharingManager.startDiscovery()
    }

    fun stopDiscovery() {
        nearbySharingManager.stopDiscovery()
    }

    fun startAdvertising() {
        nearbySharingManager.startAdvertising()
    }

    fun stopAdvertising() {
        nearbySharingManager.stopAdvertising()
    }

    fun requestConnection(endpointId: String) {
        nearbySharingManager.requestConnection(endpointId)
    }

    fun acceptConnection(endpointId: String) {
        nearbySharingManager.acceptConnection(endpointId)
    }

    fun rejectConnection(endpointId: String) {
        nearbySharingManager.rejectConnection(endpointId)
    }

    fun cancelTransfer() {
        nearbySharingManager.cancelTransfer()
    }

    fun prepareExternalShare(context: Context) {
        val payload = currentPayload ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val files = mutableListOf<File>()
                when (payload) {
                    is SharePayload.SingleTrack -> {
                        repository.getTrackById(payload.trackId)?.let { track ->
                            val f = File(track.filePath)
                            if (f.exists()) files.add(f)
                        }
                    }
                    is SharePayload.MultipleTracks -> {
                        payload.trackIds.forEach { id ->
                            repository.getTrackById(id)?.let { track ->
                                val f = File(track.filePath)
                                if (f.exists()) files.add(f)
                            }
                        }
                    }
                    is SharePayload.Artist -> {
                        repository.getArtistById(payload.artistId).first()?.let { artist ->
                            val tracks = repository.getTracksByArtist(artist.name).first()
                            tracks.forEach { track ->
                                val f = File(track.filePath)
                                if (f.exists()) files.add(f)
                            }
                        }
                    }
                    is SharePayload.Album -> {
                        repository.getAlbumById(payload.albumId).first()?.let { album ->
                            val tracks = repository.getTracksByAlbum(album.title).first()
                            tracks.forEach { track ->
                                val f = File(track.filePath)
                                if (f.exists()) files.add(f)
                            }
                        }
                    }
                    is SharePayload.Playlist -> {
                        val tracks = repository.getTracksForPlaylistById(payload.playlistId).first()
                        
                        if (tracks.isNotEmpty()) {
                            val sb = StringBuilder()
                            sb.append("#EXTM3U\n")
                            sb.append("#PLAYLIST:${payload.playlistId}\n")
                            for (track in tracks) {
                                val durationSec = track.durationMs / 1000
                                sb.append("#EXTINF:${durationSec},${track.artist} - ${track.title}\n")
                                val file = File(track.filePath)
                                sb.append("${file.name}\n")
                            }
                            
                            val sanitizedName = payload.playlistId.replace(Regex("[\\\\/:*?\"<>|]"), "_")
                            val m3uFile = File(context.cacheDir, "$sanitizedName.m3u")
                            m3uFile.writeText(sb.toString())
                            files.add(m3uFile)
                        }
                        
                        tracks.forEach { track ->
                            val f = File(track.filePath)
                            if (f.exists()) files.add(f)
                        }
                    }
                }

                if (files.isEmpty()) return@launch

                withContext(Dispatchers.Main) {
                    if (files.size > 1) {
                        android.widget.Toast.makeText(context, "Preparing ${files.size} files for sharing...", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                val fileToShare = if (files.size > 1) {
                    val zipFile = File(context.cacheDir, "shared_music_${System.currentTimeMillis()}.zip")
                    java.util.zip.ZipOutputStream(java.io.BufferedOutputStream(java.io.FileOutputStream(zipFile))).use { zos ->
                        zos.setLevel(java.util.zip.Deflater.NO_COMPRESSION)
                        files.forEach { file ->
                            if (file.exists()) {
                                val entry = java.util.zip.ZipEntry(file.name)
                                zos.putNextEntry(entry)
                                file.inputStream().buffered().use { it.copyTo(zos) }
                                zos.closeEntry()
                            }
                        }
                    }
                    zipFile
                } else {
                    files.first()
                }

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fileToShare)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (files.size > 1) "application/zip" else "audio/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    val chooser = Intent.createChooser(intent, "Share Music")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Failed to prepare share: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        nearbySharingManager.stopDiscovery()
        nearbySharingManager.stopAdvertising()
    }
}
