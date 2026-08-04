package com.aeswox.arcmusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import com.aeswox.arcmusic.db.MusicRepository
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.db.entities.Album
import com.aeswox.arcmusic.db.entities.Artist
import com.aeswox.arcmusic.db.entities.PlayHistory
import com.aeswox.arcmusic.db.entities.Playlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

sealed class SearchResultsUiState {
    object Empty : SearchResultsUiState()
    data class Success(
        val tracks: List<Track>,
        val albums: List<Album>,
        val artists: List<Artist>,
        val playlists: List<Playlist>
    ) : SearchResultsUiState()
}

// --- Listening Stats data model ---

data class ArtistStatEntry(
    val artistName: String,
    val photoUri: String?,
    val totalMinutes: Long
)

data class GenreStatEntry(
    val genre: String,
    val totalMinutes: Long
)

/**
 * All values pre-computed from PlayHistory + Track on the ViewModel side.
 * @param totalMinutes  Approximate total (each play = full track duration, skipped or not).
 * @param weekOverWeekPct  Non-null only when there is enough prior-week data for a meaningful comparison.
 * @param weeklyMinutesByDay  7 entries (Mon..Sun of current week), 0 if no plays that day.
 * @param topArtists  Ranked by approximate listening time.
 * @param topGenres  Ranked by library track count (not listening time).
 * @param nightOwlMinutesByHour  24 values of total minutes per hour-of-day — null if < 30 play events.
 */
data class ListeningStatsData(
    val totalMinutes: Long,
    val weekOverWeekPct: Int?,          // null = hide the trend line
    val weeklyMinutesByDay: List<Long>, // 7 entries Mon-Sun
    val topArtists: List<ArtistStatEntry>,
    val topGenres: List<GenreStatEntry>,
    val nightOwlMinutesByHour: List<Long> // 24 values of total minutes per hour-of-day
)

// --- Collection Health data model ---
data class DuplicateGroup(
    val title: String,
    val artist: String,
    val tracks: List<Track>
)

data class CollectionHealthState(
    val healthScore: Int = 100,
    val missingArtworkCount: Int = 0,
    val missingMetadataCount: Int = 0,
    val duplicateGroups: List<DuplicateGroup> = emptyList(),
    val corruptedTagsCount: Int = 0,
    val lowQualityCount: Int = 0,
    val missingTracksFromOwnedArtists: Int = 0,
    val missingAlbumsFromOwnedArtists: Int = 0,
    val favoritedArtistsCount: Int = 0
)

data class MissingContentItem(
    val title: String,
    val artistName: String,
    val isAlbum: Boolean,
    val imageUrl: String? = null,
    val missingCount: Int = 0
)

sealed class MissingContentUiState {
    object Loading : MissingContentUiState()
    data class Success(
        val missingAlbums: Map<String, List<MissingContentItem>>,
        val missingTracks: Map<String, List<MissingContentItem>>
    ) : MissingContentUiState()
    data class Empty(val message: String = "Nothing missing! Favorite more artists to track gaps.") : MissingContentUiState()
}

enum class ThemeMode { System, Light, Dark }

@HiltViewModel
class MusicViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val repository: MusicRepository,
    private val musicPlayerConnection: com.aeswox.arcmusic.playback.MusicPlayerConnection,
    private val lyricsRepository: com.aeswox.arcmusic.data.repository.LyricsRepository,
    private val settingsRepository: com.aeswox.arcmusic.data.SettingsRepository
) : ViewModel() {

    val randomPicks: StateFlow<List<Track>>
    val recentlyPlayed: StateFlow<List<Track>>
    val recommended: StateFlow<List<Track>>
    
    val libraryAlbums: StateFlow<List<Album>>
    val libraryArtists: StateFlow<List<Artist>>
    val libraryTracks: StateFlow<List<Track>>
    val libraryPlaylists: StateFlow<List<Playlist>>
    val favoriteTracks: StateFlow<List<Track>>
    val favoriteAlbums: StateFlow<List<Album>>
    val favoriteArtists: StateFlow<List<Artist>>
    
    val allGenres: StateFlow<List<String>>
    val recentSearches: StateFlow<List<com.aeswox.arcmusic.db.entities.SearchHistory>>
    
    data class GenreCount(val genre: String, val count: Int)
    val genreCounts: StateFlow<List<GenreCount>>
    
    val genreTopTracks: StateFlow<List<Track>>
    val genreTopAlbums: StateFlow<List<Album>>
    val genreTopArtists: StateFlow<List<Artist>>
    
    private val _healthState = MutableStateFlow(CollectionHealthState())
    val healthState: StateFlow<CollectionHealthState> = _healthState.asStateFlow()
    
    private val _missingContentUiState = MutableStateFlow<MissingContentUiState>(MissingContentUiState.Loading)
    val missingContentUiState: StateFlow<MissingContentUiState> = _missingContentUiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lyricsUiState = MutableStateFlow<com.aeswox.arcmusic.data.model.Lyrics?>(null)
    val lyricsUiState: StateFlow<com.aeswox.arcmusic.data.model.Lyrics?> = _lyricsUiState.asStateFlow()
    val currentPlaybackPosition: StateFlow<Long> = musicPlayerConnection.currentPosition

    private val _currentlyPlaying = MutableStateFlow<Track?>(null)
    val currentlyPlaying: StateFlow<Track?> = _currentlyPlaying.asStateFlow()

    init {
        viewModelScope.launch {
            currentlyPlaying.collectLatest { track ->
                if (track != null) {
                    _lyricsUiState.value = null // Reset while fetching
                    _lyricsUiState.value = lyricsRepository.getLyrics(track)
                } else {
                    _lyricsUiState.value = null
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<SearchResultsUiState> = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                kotlinx.coroutines.flow.flowOf(SearchResultsUiState.Empty)
            } else {
                kotlinx.coroutines.flow.combine(
                    repository.searchTracks(query),
                    repository.searchAlbums(query),
                    repository.searchArtists(query),
                    repository.searchPlaylists(query)
                ) { tracks, albums, artists, playlists ->
                    SearchResultsUiState.Success(tracks, albums, artists, playlists)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResultsUiState.Empty)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveRecentSearch(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repository.addRecentSearch(query.trim())
            }
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteRecentSearch(query)
        }
    }

    fun clearAllRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }
    
    val isPlaying: StateFlow<Boolean> = musicPlayerConnection.isPlaying

    val deviceVolume = musicPlayerConnection.deviceVolume
    val deviceMaxVolume = musicPlayerConnection.deviceMaxVolume

    val sleepTimerTriggerTime: StateFlow<Long> = musicPlayerConnection.sleepTimerTriggerTime
    val sleepTimerPauseWhenSongEnd: StateFlow<Boolean> = musicPlayerConnection.sleepTimerPauseWhenSongEnd

    val currentQueueIndex: StateFlow<Int> = musicPlayerConnection.currentMediaItemIndex

    val currentQueue: StateFlow<List<Track>> = musicPlayerConnection.currentQueue.map { mediaItems ->
        mediaItems.mapNotNull { mediaItem ->
            val artworkUriStr = mediaItem.mediaMetadata.artworkUri?.toString()
            val extras = mediaItem.mediaMetadata.extras
            Track(
                id = mediaItem.mediaId,
                title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown",
                artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown",
                albumArtist = null,
                albumId = null,
                album = "",
                genre = null,
                composer = null,
                year = null,
                trackNumber = null,
                discNumber = null,
                durationMs = extras?.getLong("durationMs") ?: 0L,
                filePath = "",
                fileSizeBytes = 0L,
                bitrate = null,
                codec = null,
                artworkUri = artworkUriStr,
                sampleRate = null,
                bitDepth = null,
                dateAdded = 0L,
                dateModified = 0L,
                source = com.aeswox.arcmusic.db.entities.TrackSource.LOCAL,
                isFavorite = false,
                playCount = 0,
                lastPlayedAt = null,
                remoteId = null
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _scanResult = MutableStateFlow<com.aeswox.arcmusic.db.ScanResult?>(null)
    val scanResult: StateFlow<com.aeswox.arcmusic.db.ScanResult?> = _scanResult.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _isLibraryLoaded = MutableStateFlow(false)
    val isLibraryLoaded: StateFlow<Boolean> = _isLibraryLoaded.asStateFlow()

    private val _isMiniPlayerVisible = MutableStateFlow(true)
    val isMiniPlayerVisible: StateFlow<Boolean> = _isMiniPlayerVisible.asStateFlow()

    fun setMiniPlayerVisible(visible: Boolean) {
        _isMiniPlayerVisible.value = visible
    }

    fun scanMediaStore() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val result = repository.scanMediaStore()
                repository.fetchMissingArtwork() // Fetch Deezer art for missing images
                _scanResult.value = result
            } catch (e: Exception) {
                _scanResult.value = null
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun getAlbumById(id: String): Flow<Album?> = repository.getAlbumById(id)
    fun getTracksByAlbum(albumTitle: String): Flow<List<Track>> = repository.getTracksByAlbum(albumTitle)
    fun getAlbumsByArtist(artistName: String): Flow<List<Album>> = repository.getAlbumsByArtist(artistName)
    
    fun getPlaylist(playlistName: String): Flow<Playlist?> {
        return repository.getPlaylist(playlistName)
    }

    fun getTracksForPlaylist(playlistName: String): Flow<List<Track>> {
        return repository.getTracksForPlaylist(playlistName)
    }

    fun getPlaylistsContainingTracks(trackIds: List<String>): Flow<List<String>> {
        return repository.getPlaylistsContainingTracks(trackIds)
    }

    fun getArtistById(id: String): Flow<Artist?> = repository.getArtistById(id)
    fun getTracksByArtist(artistName: String): Flow<List<Track>> = repository.getTracksByArtist(artistName)

    fun searchArtistImagesOnInternet(artistName: String): Flow<List<String>> = flow {
        emit(repository.searchArtistImages(artistName))
    }

    fun updateArtistImage(artistId: String, newUri: String) {
        viewModelScope.launch {
            repository.updateArtistPhoto(artistId, newUri)
        }
    }
    
    // Temporary EAC3 test
    private var testPlayer: androidx.media3.exoplayer.ExoPlayer? = null
    fun testEac3Playback(context: android.content.Context) {
        viewModelScope.launch {
            try {
                // Find an EAC3 file in the database
                val eac3Track = repository.getEac3Track()
                    val testFilePath = eac3Track?.filePath ?: "/sdcard/Android/data/com.aeswox.arcmusic/files/sample.eac3"
                    android.util.Log.d("EAC3_TEST", "Attempting playback on: $testFilePath")
                    testPlayer?.release()
                    testPlayer = androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                        setMediaItem(androidx.media3.common.MediaItem.fromUri(testFilePath))
                        prepare()
                        play()
                        addListener(object : androidx.media3.common.Player.Listener {
                            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                                android.util.Log.e("EAC3_TEST", "Player Error: ${error.message}", error)
                            }
                        })
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun setCurrentlyPlaying(song: Track?, contextQueue: List<Track>? = null) {
        _currentlyPlaying.value = song
        setMiniPlayerVisible(true)
        song?.filePath?.let { path ->
            val sourceList = if (!contextQueue.isNullOrEmpty()) contextQueue else listOf(song)
            
            // Limit to 300 tracks: keep up to 50 previous tracks, and the rest upcoming
            val originalIndex = sourceList.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            val startWindow = maxOf(0, originalIndex - 50)
            val endWindow = minOf(sourceList.size, startWindow + 300)
            val limitedSourceList = sourceList.subList(startWindow, endWindow)

            val queue = limitedSourceList.mapNotNull { track ->
                androidx.media3.common.MediaItem.Builder()
                    .setUri(track.filePath)
                    .setMediaId(track.id)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setArtworkUri(track.artworkUri?.let { android.net.Uri.parse(it) })
                            .setExtras(android.os.Bundle().apply {
                                putLong("durationMs", track.durationMs)
                            })
                            .build()
                    )
                    .build()
            }
            if (queue.isNotEmpty() && queue.any { it.mediaId == song.id }) {
                val startIndex = queue.indexOfFirst { it.mediaId == song.id }.coerceAtLeast(0)
                musicPlayerConnection.playQueue(queue, startIndex)
            } else {
                musicPlayerConnection.playQueue(queue, 0)
            }
        }
    }
    
    fun togglePlayPause() {
        if (musicPlayerConnection.isPlaying.value) {
            musicPlayerConnection.pause()
        } else {
            musicPlayerConnection.play()
        }
    }

    fun pause() {
        musicPlayerConnection.pause()
    }

    fun stop() {
        musicPlayerConnection.pause()
    }
    
    fun setDeviceVolume(volume: Int) {
        musicPlayerConnection.setDeviceVolume(volume)
    }
    
    fun startSleepTimer(minute: Int) {
        musicPlayerConnection.sleepTimerManager?.start(minute)
    }

    fun clearSleepTimer() {
        musicPlayerConnection.sleepTimerManager?.clear()
    }

    fun skipToNext() {
        musicPlayerConnection.skipToNext()
    }

    fun skipToPrevious() {
        musicPlayerConnection.skipToPrevious()
    }

    fun skipToQueueItem(index: Int) {
        musicPlayerConnection.skipToQueueItem(index)
    }

    val currentPosition: StateFlow<Long> = musicPlayerConnection.currentPosition
    val duration: StateFlow<Long> = musicPlayerConnection.duration

    fun deleteTracks(trackIds: List<String>) {
        viewModelScope.launch {
            repository.deleteTracks(trackIds)
            // No need to manually refresh libraryTracks, Room Flow will emit new list
        }
    }
    
    fun toggleFavorite(trackIds: List<String>, isFavorite: Boolean) {
        viewModelScope.launch {
            trackIds.forEach { id ->
                repository.toggleFavorite(id, isFavorite)
            }
        }
    }

    fun toggleAlbumFavorite(albumIds: List<String>, isFavorite: Boolean) {
        viewModelScope.launch {
            albumIds.forEach { id ->
                repository.toggleAlbumFavorite(id, isFavorite)
            }
        }
    }

    fun toggleArtistFavorite(artistIds: List<String>, isFavorite: Boolean) {
        viewModelScope.launch {
            artistIds.forEach { id ->
                repository.toggleArtistFavorite(id, isFavorite)
            }
        }
    }

    fun deleteAlbums(albumTitles: List<String>) {
        viewModelScope.launch {
            repository.deleteAlbums(albumTitles)
        }
    }

    fun deleteArtists(artistNames: List<String>) {
        viewModelScope.launch {
            repository.deleteArtists(artistNames)
        }
    }

    fun deletePlaylists(playlistTitles: List<String>) {
        viewModelScope.launch {
            repository.deletePlaylists(playlistTitles)
        }
    }

    fun createPlaylist(name: String, description: String?, coverArtUri: String?, trackIds: List<String>) {
        viewModelScope.launch {
            var finalCoverArtUri = coverArtUri
            if (coverArtUri != null && coverArtUri.startsWith("content://")) {
                try {
                    val playlistId = java.util.UUID.randomUUID().toString()
                    val destinationFile = java.io.File(context.filesDir, "playlist_covers/${playlistId}.jpg")
                    destinationFile.parentFile?.mkdirs()
                    context.contentResolver.openInputStream(android.net.Uri.parse(coverArtUri))?.use { input ->
                        java.io.FileOutputStream(destinationFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    finalCoverArtUri = destinationFile.toURI().toString() // Room will save this string
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.createPlaylist(name, description, finalCoverArtUri, trackIds)
        }
    }

    fun addTracksToPlaylist(playlistId: String, trackIds: List<String>) {
        viewModelScope.launch {
            repository.addTracksToPlaylist(playlistId, trackIds)
        }
    }

    fun loadMissingContent() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _missingContentUiState.value = MissingContentUiState.Loading
            val favoritedArtists = libraryArtists.value.filter { it.isFavorite }
            if (favoritedArtists.isEmpty()) {
                _missingContentUiState.value = MissingContentUiState.Empty()
                return@launch
            }
            
            val albums = libraryAlbums.value
            val albumMap = albums.associate { it.title to it.trackCount }
            
            val missingAlbums = mutableListOf<MissingContentItem>()
            val missingTracks = mutableListOf<MissingContentItem>()
            
            var hasAnyGaps = false

            var errorMessage = ""

            for (artist in favoritedArtists) {
                try {
                    val gaps = repository.getDetailedDiscographyGaps(artist, albumMap)
                    if (gaps == null) {
                        errorMessage += "[${artist.name}: gaps returned null] "
                    } else {
                        // Update the cached counts in DB with the accurate new logic counts
                        repository.updateArtistGaps(artist.id, gaps.first.size, gaps.second.size)

                        if (gaps.first.isNotEmpty() || gaps.second.isNotEmpty()) {
                            hasAnyGaps = true
                            missingTracks.addAll(gaps.first)
                            missingAlbums.addAll(gaps.second)
                        } else {
                            errorMessage += "[${artist.name}: 0 gaps] "
                        }
                    }
                } catch (e: Exception) {
                    errorMessage += "[${artist.name} Exc: ${e.message}] "
                }
                // Delay to respect MusicBrainz 1 request/sec rate limit
                kotlinx.coroutines.delay(1200)
            }
            
            if (!hasAnyGaps) {
                val baseMsg = "Nothing missing! Favorite more artists to track gaps."
                val debugMsg = if (errorMessage.isNotEmpty()) "\n\nDEBUG INFO:\n$errorMessage" else ""
                _missingContentUiState.value = MissingContentUiState.Empty(baseMsg + debugMsg)
            } else {
                _missingContentUiState.value = MissingContentUiState.Success(
                    missingAlbums = missingAlbums.groupBy { it.artistName },
                    missingTracks = missingTracks.groupBy { it.artistName }
                )
            }
        }
    }

    fun seekTo(positionFraction: Float) {
        val dur = duration.value
        if (dur > 0) {
            musicPlayerConnection.seekTo((positionFraction * dur).toLong())
        }
    }

    val listeningStats: StateFlow<ListeningStatsData>

    init {
        randomPicks = repository.getRandomTracks(20).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        recentlyPlayed = repository.getRecentlyPlayedTracks(4).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        recommended = repository.getRandomTracks(10).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        
        libraryAlbums = repository.getAllAlbums().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        allGenres = repository.getAllGenres()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            
        genreCounts = repository.getAllTracks().map { tracks ->
            tracks.mapNotNull { it.genre?.trim()?.takeIf { g -> g.isNotEmpty() } }
                .groupingBy { it }
                .eachCount()
                .entries
                .map { GenreCount(it.key, it.value) }
                .sortedByDescending { it.count }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        recentSearches = repository.getRecentSearches(5)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        val genreFilter = MutableStateFlow<String?>(null)
        libraryArtists = repository.getAllArtists().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        libraryPlaylists = repository.getAllPlaylists().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        libraryTracks = repository.getAllTracks()
            .onEach { 
                if (!_isLibraryLoaded.value) {
                    kotlinx.coroutines.delay(150)
                    _isLibraryLoaded.value = true 
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            
        favoriteTracks = libraryTracks.map { tracks -> tracks.filter { it.isFavorite } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        favoriteAlbums = libraryAlbums.map { albums -> albums.filter { it.isFavorite } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        favoriteArtists = libraryArtists.map { artists -> artists.filter { it.isFavorite } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        
        genreTopTracks = repository.getRandomTracks(10).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        genreTopAlbums = repository.getAllAlbums().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        genreTopArtists = repository.getAllArtists().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        // --- Listening Stats: computed from PlayHistory + Track library ---
        listeningStats = combine(
            repository.getFullPlayHistory(),
            repository.getAllTracks()
        ) { history, tracks ->
            computeListeningStats(history, tracks)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ListeningStatsData(0L, null, List(7) { 0L }, emptyList(), emptyList(), List(24) { 0L })
        )

        // --- Collection Health computation ---
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            combine(
                combine(
                    repository.getTracksMissingArtwork(),
                    repository.getTracksMissingMetadata(),
                    repository.getLowQualityTracks()
                ) { a, b, c -> Triple(a, b, c) },
                combine(
                    repository.getCorruptedTracks(),
                    libraryTracks,
                    libraryArtists
                ) { a, b, c -> Triple(a, b, c) }
            ) { triple1, triple2 ->
                val missingArtwork = triple1.first
                val missingMetadata = triple1.second
                val lowQuality = triple1.third
                val corrupted = triple2.first
                val tracks = triple2.second
                val artists = triple2.third

                if (tracks.isEmpty()) {
                    return@combine CollectionHealthState()
                }

                // Calculate duplicates (Group by title + artist, then fuzzy duration ±5s)
                val duplicateGroups = tracks.groupBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }
                    .filter { it.value.size > 1 }
                    .map { entry -> 
                        val groupedByDuration = entry.value.groupBy { it.durationMs / 5000 }
                        groupedByDuration.values.filter { it.size > 1 }.map { duplicates ->
                            DuplicateGroup(
                                title = duplicates.first().title,
                                artist = duplicates.first().artist,
                                tracks = duplicates
                            )
                        }
                    }
                    .flatten()

                val trackCount = tracks.size.coerceAtLeast(1).toFloat()
                
                // Score weighting: 25% each (Artwork, Metadata, Duplicates, Quality)
                val healthScore = 100 - (
                    (minOf(25f, (missingArtwork.size.toFloat() / trackCount) * 25f)) +
                    (minOf(25f, (missingMetadata.size.toFloat() / trackCount) * 25f)) +
                    (minOf(25f, (duplicateGroups.size.toFloat() / trackCount) * 25f)) +
                    (minOf(25f, (lowQuality.size.toFloat() / trackCount) * 25f))
                ).toInt()

                val favoritedArtists = artists.filter { it.isFavorite }
                val missingTracks = favoritedArtists.sumOf { it.missingTracksCount ?: 0 }
                val missingAlbums = favoritedArtists.sumOf { it.missingAlbumsCount ?: 0 }

                CollectionHealthState(
                    healthScore = healthScore.coerceIn(0, 100),
                    missingArtworkCount = missingArtwork.size,
                    missingMetadataCount = missingMetadata.size,
                    duplicateGroups = duplicateGroups,
                    corruptedTagsCount = corrupted.size,
                    lowQualityCount = lowQuality.size,
                    missingTracksFromOwnedArtists = missingTracks,
                    missingAlbumsFromOwnedArtists = missingAlbums,
                    favoritedArtistsCount = favoritedArtists.size
                )
            }.collectLatest { state ->
                _healthState.value = state
            }
        }
        
        // Background sync for discography gaps
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            libraryArtists.collectLatest { artists ->
                val albums = libraryAlbums.value
                val albumMap = albums.associate { it.title to it.trackCount }
                artists.filter { it.isFavorite }.forEach { artist ->
                    if (artist.missingTracksCount == null || artist.missingAlbumsCount == null) {
                        val gaps = repository.fetchDiscographyGaps(artist.name, albumMap)
                        if (gaps != null) {
                            repository.updateArtistGaps(artist.id, gaps.first, gaps.second)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            musicPlayerConnection.currentlyPlayingItem.collect { mediaItem ->
                if (mediaItem != null) {
                    var currentTrack = randomPicks.value.find { it.id == mediaItem.mediaId }
                        ?: recentlyPlayed.value.find { it.id == mediaItem.mediaId }
                        ?: libraryTracks.value.find { it.id == mediaItem.mediaId }
                        
                    if (currentTrack == null) {
                        val artworkUriStr = mediaItem.mediaMetadata.artworkUri?.toString()
                        val extras = mediaItem.mediaMetadata.extras
                        currentTrack = Track(
                            id = mediaItem.mediaId,
                            title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown",
                            artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown",
                            albumArtist = null,
                            albumId = null,
                            album = "",
                            genre = null,
                            composer = null,
                            year = null,
                            trackNumber = null,
                            discNumber = null,
                            durationMs = extras?.getLong("durationMs") ?: 0L,
                            filePath = "",
                            fileSizeBytes = 0L,
                            bitrate = null,
                            codec = null,
                            artworkUri = artworkUriStr,
                            sampleRate = null,
                            bitDepth = null,
                            dateAdded = 0L,
                            dateModified = 0L,
                            source = com.aeswox.arcmusic.db.entities.TrackSource.LOCAL,
                            isFavorite = false,
                            playCount = 0,
                            lastPlayedAt = null,
                            remoteId = null
                        )
                    }
                    if (_currentlyPlaying.value?.id != currentTrack.id) {
                        _currentlyPlaying.value = currentTrack
                    }
                } else {
                    _currentlyPlaying.value = null
                }
            }
        }
    }

    private val _themeMode = MutableStateFlow(ThemeMode.Light)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _tintTransparency = MutableStateFlow(0.4f)
    val tintTransparency: StateFlow<Float> = _tintTransparency.asStateFlow()
    
    private val _noiseFactor = MutableStateFlow(0.06f)
    val noiseFactor: StateFlow<Float> = _noiseFactor.asStateFlow()
    
    private val _glowIntensity = MutableStateFlow(0.6f)
    val glowIntensity: StateFlow<Float> = _glowIntensity.asStateFlow()
    
    private val _lightThemeForNowPlaying = MutableStateFlow(false)
    val lightThemeForNowPlaying: StateFlow<Boolean> = _lightThemeForNowPlaying.asStateFlow()
    
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setTintTransparency(value: Float) {
        _tintTransparency.value = value
    }
    
    fun setNoiseFactor(value: Float) {
        _noiseFactor.value = value
    }

    fun setGlowIntensity(value: Float) {
        _glowIntensity.value = value
    }
    
    fun setLightThemeForNowPlaying(value: Boolean) {
        _lightThemeForNowPlaying.value = value
    }
    
    val lastFmApiKey = settingsRepository.lastFmApiKey.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun setLastFmApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setLastFmApiKey(key)
        }
    }


    // ---------------------------------------------------------------------------
    // Stats computation — pure function, called inside combine() on IO thread.
    // ---------------------------------------------------------------------------

    private fun getEffectivePlayedMs(ph: PlayHistory, track: Track?): Long {
        return when {
            ph.playedMs > 0L -> ph.playedMs
            ph.playedMs == -1L -> 0L // Currently playing or skipped immediately
            else -> track?.durationMs ?: 0L // Legacy row fallback
        }
    }

    private fun computeListeningStats(
        history: List<PlayHistory>,
        tracks: List<Track>
    ): ListeningStatsData {
        if (history.isEmpty() || tracks.isEmpty()) {
            return ListeningStatsData(
                totalMinutes = 0,
                weekOverWeekPct = null,
                weeklyMinutesByDay = List(7) { 0L },
                topArtists = emptyList(),
                topGenres = computeTopGenresByCount(tracks),
                nightOwlMinutesByHour = List(24) { 0L }
            )
        }

        val trackById: Map<String, Track> = tracks.associateBy { it.id }

        // --- Total listening time (actual played ms, not full duration) ---
        val totalMs = history.sumOf { ph -> getEffectivePlayedMs(ph, trackById[ph.trackId]) }
        val totalMinutes = totalMs / 60_000L

        // --- Week-over-week trend ---
        val now = System.currentTimeMillis()
        val thisWeekStart = now - 7L * 24 * 3600 * 1000
        val prevWeekStart = now - 14L * 24 * 3600 * 1000

        val thisWeekMs = history
            .filter { it.timestamp >= thisWeekStart }
            .sumOf { ph -> getEffectivePlayedMs(ph, trackById[ph.trackId]) }

        // Only show the trend if we have actual history in the *prior* week
        val prevWeekHistory = history.filter { it.timestamp in prevWeekStart until thisWeekStart }
        val weekOverWeekPct: Int? = if (prevWeekHistory.isEmpty()) {
            null // Not enough history yet — hide the line
        } else {
            val prevWeekMs = prevWeekHistory.sumOf { ph -> getEffectivePlayedMs(ph, trackById[ph.trackId]) }
            if (prevWeekMs == 0L) null
            else ((thisWeekMs - prevWeekMs) * 100L / prevWeekMs).toInt()
        }

        // --- Weekly activity: minutes per day of current 7-day window (Mon..Sun) ---
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = now
        // Roll back to start of today
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val dayMs = 24L * 3600 * 1000

        // Build 7 buckets: index 0 = 6 days ago, index 6 = today
        val weeklyMinutesByDay = (6 downTo 0).map { daysAgo ->
            val dayStart = cal.timeInMillis - daysAgo * dayMs
            val dayEnd = dayStart + dayMs
            history
                .filter { it.timestamp in dayStart until dayEnd }
                .sumOf { ph -> getEffectivePlayedMs(ph, trackById[ph.trackId]) } / 60_000L
        }

        // --- Top Artists (by approximate listening time) ---
        val artistMinutes = mutableMapOf<String, Long>()
        history.forEach { ph ->
            val track = trackById[ph.trackId] ?: return@forEach
            val artist = track.artist.ifBlank { return@forEach }
            val playedMin = getEffectivePlayedMs(ph, track) / 60_000L
            artistMinutes[artist] = (artistMinutes[artist] ?: 0L) + playedMin
        }
        // Build artist entries — photoUri comes from the Artists table via libraryArtists,
        // but since we only have Track here, we leave photoUri null (placeholder shown in UI).
        val topArtists = artistMinutes.entries
            .sortedByDescending { it.value }
            .take(8)
            .map { (name, minutes) -> ArtistStatEntry(name, null, minutes) }

        // --- Top Genres (ranked by actual listening time) ---
        val topGenres = computeTopGenresByListeningTime(history, trackById)

        // --- Listening Personality: computed from any available history ---
        val nightOwlData: List<Long> = run {
            val minutesByHour = LongArray(24)
            history.forEach { ph ->
                val track = trackById[ph.trackId]
                val durationMin = getEffectivePlayedMs(ph, track) / 60_000L
                val hourCal = java.util.Calendar.getInstance()
                hourCal.timeInMillis = ph.timestamp
                val hour = hourCal.get(java.util.Calendar.HOUR_OF_DAY)
                minutesByHour[hour] += durationMin
            }
            minutesByHour.toList()
        }

        return ListeningStatsData(
            totalMinutes = totalMinutes,
            weekOverWeekPct = weekOverWeekPct,
            weeklyMinutesByDay = weeklyMinutesByDay,
            topArtists = topArtists,
            topGenres = topGenres,
            nightOwlMinutesByHour = nightOwlData
        )
    }

    /** Ranks genres by total listening minutes accumulated in play history. */
    private fun computeTopGenresByListeningTime(
        history: List<PlayHistory>,
        trackById: Map<String, Track>
    ): List<GenreStatEntry> {
        val genreMinutes = mutableMapOf<String, Long>()
        history.forEach { ph ->
            val track = trackById[ph.trackId] ?: return@forEach
            val genre = track.genre?.trim()?.takeIf { it.isNotEmpty() } ?: return@forEach
            val playedMin = getEffectivePlayedMs(ph, track) / 60_000L
            genreMinutes[genre] = (genreMinutes[genre] ?: 0L) + playedMin
        }
        return genreMinutes.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { (genre, minutes) -> GenreStatEntry(genre, minutes) }
    }

    /** Fallback: rank genres by number of tracks in the library (used when no history exists). */
    private fun computeTopGenresByCount(tracks: List<Track>): List<GenreStatEntry> {
        return tracks
            .mapNotNull { it.genre?.trim()?.takeIf { g -> g.isNotEmpty() } }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(4)
            .map { GenreStatEntry(it.key, 0L) }
    }
}
