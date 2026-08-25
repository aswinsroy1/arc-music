package com.aeswox.arcmusic.sharing

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import com.aeswox.arcmusic.MusicViewModel // Needed for importM3uPlaylist if available from a static context, or we can broadcast it. But actually MusicViewModel is a ViewModel. We might need a different way to import.
import com.aeswox.arcmusic.db.MusicRepository
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import android.content.ContentValues
import android.provider.MediaStore
import android.content.Intent

enum class SharingState {
    IDLE,
    ADVERTISING,
    DISCOVERING,
    CONNECTED,
    TRANSFERRING,
    ERROR
}

data class DiscoveredEndpoint(val id: String, val name: String)
data class ConnectionRequest(val endpointId: String, val endpointName: String, val authCode: String)

@Singleton
class NearbySharingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
    private val importMediaUseCase: ImportMediaUseCase
) {
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val strategy = Strategy.P2P_STAR
    private val serviceId = "com.aeswox.arcmusic.SERVICE_ID"
    private val userName = android.os.Build.MODEL // Use device name
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _sharingState = MutableStateFlow(SharingState.IDLE)
    val sharingState: StateFlow<SharingState> = _sharingState.asStateFlow()

    private val _transferProgress = MutableStateFlow(0f)
    val transferProgress: StateFlow<Float> = _transferProgress.asStateFlow()

    private val _discoveredEndpoints = MutableStateFlow<List<DiscoveredEndpoint>>(emptyList())
    val discoveredEndpoints: StateFlow<List<DiscoveredEndpoint>> = _discoveredEndpoints.asStateFlow()

    private val _connectionRequest = MutableStateFlow<ConnectionRequest?>(null)
    val connectionRequest: StateFlow<ConnectionRequest?> = _connectionRequest.asStateFlow()

    private var currentPayload: SharePayload? = null

    // Store expected metadata by Payload ID (for receiving)
    private val expectedMetadata = mutableMapOf<Long, JSONObject>()
    // Store sent payloads to track when to delete temp zips
    private val sentFiles = mutableMapOf<Long, File>()

    fun setPayload(payload: SharePayload) {
        currentPayload = payload
    }

    private fun updateTransferService(state: SharingState, progress: Float = 0f) {
        val intent = Intent(context, NearbyTransferService::class.java)
        if (state == SharingState.TRANSFERRING) {
            intent.putExtra("PROGRESS", (progress * 100).toInt())
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            intent.action = "STOP"
            context.startService(intent)
        }
    }

    private val incomingFiles = mutableMapOf<Long, File>()
    private val incomingUris = mutableMapOf<Long, android.net.Uri>()
    private val activePayloads = mutableSetOf<Long>()

    private val actualPayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            activePayloads.add(payload.id)
            _sharingState.value = SharingState.TRANSFERRING
            updateTransferService(SharingState.TRANSFERRING, 0f)
            
            if (payload.type == Payload.Type.BYTES) {
                val jsonStr = String(payload.asBytes()!!, Charsets.UTF_8)
                val json = JSONObject(jsonStr)
                if (json.has("payloadId")) {
                    expectedMetadata[json.getLong("payloadId")] = json
                }
            } else if (payload.type == Payload.Type.FILE) {
                payload.asFile()?.asUri()?.let { uri ->
                    incomingUris[payload.id] = uri
                } ?: payload.asFile()?.asJavaFile()?.let { file ->
                    incomingFiles[payload.id] = file
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.IN_PROGRESS) {
                _sharingState.value = SharingState.TRANSFERRING
                if (update.totalBytes > 0) {
                    val progress = update.bytesTransferred.toFloat() / update.totalBytes.toFloat()
                    _transferProgress.value = progress
                    updateTransferService(SharingState.TRANSFERRING, progress)
                }
            } else if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                activePayloads.remove(update.payloadId)
                if (activePayloads.isEmpty()) {
                    _sharingState.value = SharingState.IDLE
                    _transferProgress.value = 0f
                    updateTransferService(SharingState.IDLE)
                }
                
                sentFiles[update.payloadId]?.let { file ->
                    if (file.exists() && file.name.endsWith(".zip")) {
                        file.delete()
                    }
                    sentFiles.remove(update.payloadId)
                }

                val receivedUri = incomingUris[update.payloadId]
                val receivedFile = incomingFiles[update.payloadId]
                
                if (receivedUri != null || receivedFile != null) {
                    val metadata = expectedMetadata[update.payloadId]
                    if (metadata != null) {
                        coroutineScope.launch {
                            importMediaUseCase.processReceivedPayload(receivedUri, receivedFile, metadata)
                        }
                        expectedMetadata.remove(update.payloadId)
                    }
                    incomingUris.remove(update.payloadId)
                    incomingFiles.remove(update.payloadId)
                }
            } else if (update.status == PayloadTransferUpdate.Status.FAILURE || update.status == PayloadTransferUpdate.Status.CANCELED) {
                incomingUris.remove(update.payloadId)
                incomingFiles[update.payloadId]?.let { file ->
                    if (file.exists()) file.delete()
                }
                incomingFiles.remove(update.payloadId)
                
                sentFiles[update.payloadId]?.let { file ->
                    if (file.exists() && file.name.endsWith(".zip")) {
                        file.delete()
                    }
                }
                sentFiles.remove(update.payloadId)
                
                activePayloads.remove(update.payloadId)
                if (activePayloads.isEmpty()) {
                    _sharingState.value = SharingState.IDLE
                    updateTransferService(SharingState.IDLE)
                }
            }
        }
    }



    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            _connectionRequest.value = ConnectionRequest(endpointId, info.endpointName, info.authenticationToken)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                _sharingState.value = SharingState.CONNECTED
                stopDiscovery()
                stopAdvertising()
                
                // If we have a payload to send, start preparing and sending it
                currentPayload?.let { payload ->
                    sendPayloadPackage(endpointId, payload)
                }
            } else {
                _sharingState.value = SharingState.ERROR
                updateTransferService(SharingState.ERROR)
            }
        }

        override fun onDisconnected(endpointId: String) {
            _sharingState.value = SharingState.IDLE
            _transferProgress.value = 0f
            updateTransferService(SharingState.IDLE)
        }
    }

    private fun sendPayloadPackage(endpointId: String, payload: SharePayload) {
        coroutineScope.launch {
            try {
                when (payload) {
                    is SharePayload.SingleTrack -> {
                        val track = repository.getTrackById(payload.trackId)
                        if (track != null) {
                            sendTrackFile(endpointId, track)
                        }
                    }
                    is SharePayload.MultipleTracks -> {
                        for (id in payload.trackIds) {
                            val track = repository.getTrackById(id)
                            if (track != null) {
                                sendTrackFile(endpointId, track)
                            }
                        }
                    }
                    is SharePayload.Artist -> {
                        val artist = repository.getArtistById(payload.artistId).first()
                        if (artist != null) {
                            val tracks = repository.getTracksByArtist(artist.name).first()
                            for (track in tracks) {
                                sendTrackFile(endpointId, track)
                            }
                        }
                    }
                    is SharePayload.Playlist -> {
                        val tracks = repository.getTracksForPlaylistById(payload.playlistId).first()
                        if (tracks.isNotEmpty()) {
                            val sb = StringBuilder()
                            sb.append("#EXTM3U\n")
                            for (track in tracks) {
                                val durationSec = track.durationMs / 1000
                                sb.append("#EXTINF:${durationSec},${track.artist} - ${track.title}\n")
                                val file = File(track.filePath)
                                sb.append("${file.name}\n")
                            }
                            
                            val m3uFile = File(context.cacheDir, "playlist_${System.currentTimeMillis()}.m3u")
                            m3uFile.writeText(sb.toString())
                            val m3uPayload = Payload.fromFile(m3uFile)
                            
                            val m3uMetadata = JSONObject().apply {
                                put("type", "playlist_m3u")
                                put("payloadId", m3uPayload.id)
                            }
                            
                            val metadataPayload = Payload.fromBytes(m3uMetadata.toString().toByteArray(Charsets.UTF_8))
                            activePayloads.add(metadataPayload.id)
                            activePayloads.add(m3uPayload.id)
                            _sharingState.value = SharingState.TRANSFERRING
                            
                            connectionsClient.sendPayload(endpointId, metadataPayload)
                            connectionsClient.sendPayload(endpointId, m3uPayload)
                            
                            for (track in tracks) {
                                sendTrackFile(endpointId, track)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NearbySharingManager", "Failed to send payload", e)
            }
        }
    }

    private fun sendTrackFile(endpointId: String, track: com.aeswox.arcmusic.db.entities.Track) {
        val file = File(track.filePath)
        if (file.exists()) {
            val filePayload = Payload.fromFile(file)
            val metadata = JSONObject().apply {
                put("payloadId", filePayload.id)
                put("type", "track")
                put("title", track.title)
                put("ext", file.extension)
                put("filename", file.name)
            }
            val metadataPayload = Payload.fromBytes(metadata.toString().toByteArray(Charsets.UTF_8))
            activePayloads.add(metadataPayload.id)
            activePayloads.add(filePayload.id)
            _sharingState.value = SharingState.TRANSFERRING
            
            connectionsClient.sendPayload(endpointId, metadataPayload)
            connectionsClient.sendPayload(endpointId, filePayload)
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val currentList = _discoveredEndpoints.value.toMutableList()
            currentList.add(DiscoveredEndpoint(endpointId, info.endpointName))
            _discoveredEndpoints.value = currentList
        }

        override fun onEndpointLost(endpointId: String) {
            val currentList = _discoveredEndpoints.value.toMutableList()
            currentList.removeAll { it.id == endpointId }
            _discoveredEndpoints.value = currentList
        }
    }

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startAdvertising(
            userName, serviceId, connectionLifecycleCallback, advertisingOptions
        ).addOnSuccessListener {
            _sharingState.value = SharingState.ADVERTISING
        }.addOnFailureListener {
            _sharingState.value = SharingState.ERROR
            Log.e("NearbySharingManager", "Failed to start advertising", it)
        }
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        if (_sharingState.value == SharingState.ADVERTISING) {
            _sharingState.value = SharingState.IDLE
        }
    }

    fun startDiscovery() {
        _discoveredEndpoints.value = emptyList()
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(
            serviceId, endpointDiscoveryCallback, discoveryOptions
        ).addOnSuccessListener {
            _sharingState.value = SharingState.DISCOVERING
        }.addOnFailureListener {
            _sharingState.value = SharingState.ERROR
            Log.e("NearbySharingManager", "Failed to start discovery", it)
        }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        if (_sharingState.value == SharingState.DISCOVERING) {
            _sharingState.value = SharingState.IDLE
        }
    }

    fun requestConnection(endpointId: String) {
        connectionsClient.requestConnection(userName, endpointId, connectionLifecycleCallback)
            .addOnFailureListener {
                _sharingState.value = SharingState.ERROR
                Log.e("NearbySharingManager", "Failed to request connection", it)
            }
    }

    fun acceptConnection(endpointId: String) {
        connectionsClient.acceptConnection(endpointId, actualPayloadCallback)
        _connectionRequest.value = null
    }

    fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
        _connectionRequest.value = null
    }

    fun disconnect(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
        _sharingState.value = SharingState.IDLE
        _transferProgress.value = 0f
        updateTransferService(SharingState.IDLE)
    }

    fun cancelTransfer() {
        val payloadIds = mutableSetOf<Long>()
        payloadIds.addAll(expectedMetadata.keys)
        payloadIds.addAll(sentFiles.keys)
        payloadIds.addAll(incomingFiles.keys)
        for (id in payloadIds) {
            connectionsClient.cancelPayload(id)
        }
        _sharingState.value = SharingState.IDLE
        _transferProgress.value = 0f
        updateTransferService(SharingState.IDLE)
    }
}
