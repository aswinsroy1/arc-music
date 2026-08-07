package com.aeswox.arcmusic.db

import com.aeswox.arcmusic.db.daos.*
import com.aeswox.arcmusic.db.entities.*
import com.aeswox.arcmusic.data.MediaStoreScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.aeswox.arcmusic.utils.ArtistUtils

class MusicRepository(
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
    private val playHistoryDao: PlayHistoryDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val missingContentDao: MissingContentDao,
    private val newReleaseDao: NewReleaseDao,
    private val dismissedCardDao: DismissedCardDao,
    private val discoveryDao: DiscoveryDao,
    private val mediaStoreScanner: MediaStoreScanner,
    private val artworkRepository: com.aeswox.arcmusic.data.network.ArtworkRepository
) {
    // Dedicated scope for background tasks — survives ViewModel but is still structured
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val inFlightGapsFetches = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.Deferred<Triple<List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>>?>>()
    private var lastFmApiKeyCache: String? = null

    init {
        // Run a one-time background backfill for Collection Growth caches if needed
        backgroundScope.launch {
            try {
                // We don't have the lastFmApiKey directly here without SettingsRepository, 
                // but we can at least fetch New Releases. 
                // Wait, lastFmApiKey is passed to loadCollectionGrowthData. 
                // A better place for backfill that requires API keys might be in MusicViewModel, 
                // or we can just fetch what we can here, and Discovery later.
                // For now, let's just initialize the variable and we will trigger the actual backfill from a method.
            } catch (e: Exception) {}
        }
    }
    
    // 7 days in milliseconds — staleness policy for cached missing content
    private val CACHE_STALE_MS = 7L * 24 * 60 * 60 * 1000
    fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks()
    fun getAllAlbums(): Flow<List<Album>> = albumDao.getAllAlbums()
    fun getAllArtists(): Flow<List<Artist>> = artistDao.getAllArtists()
    fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>> = trackDao.getRecentlyPlayedTracks(limit)
    fun getRandomTracks(limit: Int): Flow<List<Track>> = trackDao.getRandomTracks(limit)
    fun getFullPlayHistory(): Flow<List<PlayHistory>> = playHistoryDao.getFullPlayHistory()

    /**
     * Returns up to [limit] Artist entities ranked by accumulated listening time (minutes).
     * Uses the same aggregation as the Listening Stats "Top Artists" computation.
     * Artists whose names aren't in the artists table are silently excluded.
     */
    suspend fun getTopListenedArtists(limit: Int = 10): List<Artist> = withContext(Dispatchers.IO) {
        val history = playHistoryDao.getFullPlayHistory().first()
        if (history.isEmpty()) return@withContext emptyList()

        val trackById = trackDao.getAllTracks().first().associateBy { it.id }
        val artistMinutes = mutableMapOf<String, Long>()
        history.forEach { ph ->
            val track = trackById[ph.trackId] ?: return@forEach
            val artist = track.artist.ifBlank { return@forEach }
            val playedMin = when {
                ph.playedMs > 0L -> ph.playedMs
                ph.playedMs == -1L -> 0L
                else -> track.durationMs
            } / 60_000L
            artistMinutes[artist] = (artistMinutes[artist] ?: 0L) + playedMin
        }

        val topNames = artistMinutes.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
            .toSet()

        if (topNames.isEmpty()) return@withContext emptyList()

        artistDao.getAllArtists().first().filter { it.name in topNames }
    }

    fun getAlbumById(id: String): Flow<Album?> = albumDao.getAlbumById(id)
    fun getTracksByAlbum(albumTitle: String): Flow<List<Track>> = trackDao.getTracksByAlbum(albumTitle)
    fun getAlbumsByArtist(artistName: String): Flow<List<Album>> = albumDao.getAllAlbums().map { albums ->
        albums.filter { album ->
            ArtistUtils.splitArtists(album.artist).contains(artistName)
        }
    }
    
    fun getArtistById(id: String): Flow<Artist?> = artistDao.getArtistById(id)
    fun getTracksByArtist(artistName: String): Flow<List<Track>> = trackDao.getAllTracks().map { tracks ->
        tracks.filter { track ->
            ArtistUtils.splitArtists(track.artist).contains(artistName) ||
            ArtistUtils.splitArtists(track.albumArtist).contains(artistName)
        }
    }

    fun getPlaylist(playlistName: String): Flow<Playlist?> = playlistDao.getPlaylist(playlistName)
    fun getTracksForPlaylist(playlistName: String): Flow<List<Track>> = playlistDao.getTracksForPlaylist(playlistName)


    suspend fun scanMediaStore(): ScanResult = withContext(Dispatchers.IO) {
        val scannedTracks = mediaStoreScanner.scanAudioFiles()
        
        val existingTracks = trackDao.getAllTracks().first().associateBy { it.id }
        val existingAlbums = albumDao.getAllAlbums().first().associateBy { it.title }
        val existingArtists = artistDao.getAllArtists().first().associateBy { it.id }
        
        val tracks = scannedTracks.map {
            val existing = existingTracks[it.id]
            Track(
                id = it.id,
                title = it.title,
                artist = it.artist,
                albumArtist = it.albumArtist,
                albumId = it.albumId,
                album = it.album,
                genre = it.genre,
                composer = "", // Not pulled from MediaStore currently
                year = 0, // Not pulled from MediaStore
                trackNumber = it.trackNumber,
                discNumber = it.discNumber,
                durationMs = it.durationMs,
                filePath = it.filePath,
                fileSizeBytes = it.fileSizeBytes,
                bitrate = it.bitrate,
                codec = it.mimeType, // Use mimeType from MediaStore
                sampleRate = it.sampleRate, 
                bitDepth = it.bitDepth,
                dateAdded = it.dateAdded,
                dateModified = it.dateModified,
                isFavorite = existing?.isFavorite ?: false,
                playCount = existing?.playCount ?: 0,
                lastPlayedAt = existing?.lastPlayedAt,
                source = TrackSource.LOCAL,
                remoteId = existing?.remoteId,
                artworkUri = existing?.artworkUri ?: it.artworkUri
            )
        }
        
        val albums = scannedTracks.distinctBy { it.album }.map {
            val existing = existingAlbums[it.album]
            Album(
                id = it.album, // Fallback ID; could generate a better one or hash
                title = it.album,
                artist = it.albumArtist,
                year = 0,
                artworkUri = existing?.artworkUri ?: it.artworkUri,
                trackCount = scannedTracks.count { track -> track.album == it.album },
                isFavorite = existing?.isFavorite ?: false
            )
        }
        
        val artists = scannedTracks.flatMap { 
            ArtistUtils.splitArtists(it.artist) + ArtistUtils.splitArtists(it.albumArtist)
        }
            .filter { it.isNotEmpty() }
            .distinct()
            .map {
                val existing = existingArtists[it]
                Artist(
                    id = it,
                    name = it,
                    photoUri = existing?.photoUri,
                    bioText = existing?.bioText,
                    isFavorite = existing?.isFavorite ?: false,
                    missingTracksCount = existing?.missingTracksCount,
                    missingAlbumsCount = existing?.missingAlbumsCount,
                    hasScannedMissingContent = existing?.hasScannedMissingContent ?: false
                )
            }
            
        trackDao.insertTracks(tracks)
        albumDao.insertAlbums(albums)
        artistDao.insertArtists(artists)
        
        ScanResult(tracks.size, albums.size, artists.size)
    }
    
    suspend fun logPlayStart(trackId: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        playHistoryDao.insertPlayHistory(
            PlayHistory(
                trackId = trackId,
                timestamp = now,
                playedMs = -1L,
                completed = false,
                skipReason = null
            )
        )
        trackDao.incrementPlayCountAndUpdateLastPlayed(trackId, now)
    }

    suspend fun markPlayCompleted(trackId: String, playedMs: Long) = withContext(Dispatchers.IO) {
        playHistoryDao.markMostRecentCompleted(trackId, playedMs)
    }

    suspend fun updatePlayedMs(trackId: String, playedMs: Long) = withContext(Dispatchers.IO) {
        playHistoryDao.updatePlayedMs(trackId, playedMs)
    }

    suspend fun deleteTracks(trackIds: List<String>) = withContext(Dispatchers.IO) {
        trackDao.deleteTracks(trackIds)
    }

    suspend fun deleteAlbums(albumTitles: List<String>) = withContext(Dispatchers.IO) {
        albumDao.deleteAlbums(albumTitles)
    }

    suspend fun deleteArtists(artistNames: List<String>) = withContext(Dispatchers.IO) {
        artistDao.deleteArtists(artistNames)
    }
    
    suspend fun toggleFavorite(trackId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        trackDao.updateTrackFavoriteStatus(trackId, isFavorite)
    }

    suspend fun toggleAlbumFavorite(albumId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        albumDao.updateAlbumFavoriteStatus(albumId, isFavorite)
    }

    suspend fun toggleArtistFavorite(artistId: String, isFavorite: Boolean, lastFmApiKey: String? = null) = withContext(Dispatchers.IO) {
        artistDao.updateArtistFavoriteStatus(artistId, isFavorite)
        if (isFavorite) {
            val artist = artistDao.getArtistById(artistId).first()
            if (artist != null) {
                // Always force a fresh scan when an artist is (re)favorited.
                // This ensures bad cached data gets cleared immediately.
                backgroundScope.launch {
                    try {
                        android.util.Log.i("MusicRepository", "Background scan started for ${artist.name}")
                        refreshArtistGrowthData(artist, lastFmApiKey, forceRefresh = true)
                        android.util.Log.i("MusicRepository", "Background scan complete for ${artist.name}")
                    } catch (e: Exception) {
                        android.util.Log.e("MusicRepository", "Background missing content scan failed for ${artist.name}", e)
                    }
                }
            }
        }
    }

    suspend fun updateArtistGaps(artistId: String, missingTracks: Int, missingAlbums: Int) = withContext(Dispatchers.IO) {
        artistDao.updateArtistGaps(artistId, missingTracks, missingAlbums)
    }

    suspend fun fetchDiscographyGaps(artistName: String, localAlbums: Map<String, Int>): Pair<Int, Int>? {
        return artworkRepository.fetchDiscographyGaps(artistName, localAlbums)
    }

    suspend fun refreshArtistGrowthData(artist: Artist, lastFmApiKey: String?, forceRefresh: Boolean = false) {
        // 1. Discography Gaps
        getDetailedDiscographyGaps(artist, forceRefresh)

        // 2. New Releases
        val localAlbumTitles = albumDao.getAllAlbums().first()
            .filter { ArtistUtils.splitArtists(it.artist).contains(artist.name) }
            .map { it.title.lowercase() }.toSet()
            
        val newReleaseCachedAt = newReleaseDao.getOldestCachedAt(artist.name) ?: 0L
        val newReleaseIsStale = (System.currentTimeMillis() - newReleaseCachedAt) > CACHE_STALE_MS
        if (forceRefresh || newReleaseIsStale) {
            fetchAndCacheNewReleases(artist.name, localAlbumTitles, forceRefresh = true)
        }

        // 3. Discovery
        if (!lastFmApiKey.isNullOrBlank()) {
            val discoveryCachedAt = discoveryDao.getOldestCachedAt(artist.name) ?: 0L
            val discoveryIsStale = (System.currentTimeMillis() - discoveryCachedAt) > CACHE_STALE_MS
            if (forceRefresh || discoveryIsStale) {
                try {
                    val allAlbums = albumDao.getAllAlbums().first()
                    val allTracks = trackDao.getAllTracks().first()
                    val favoritedArtists = artistDao.getAllArtists().first().filter { it.isFavorite }
                    val localArtistNamesLower = allTracks.map { it.artist.lowercase() }.toSet() +
                        allAlbums.map { it.artist.lowercase() }.toSet() +
                        favoritedArtists.map { it.name.lowercase() }.toSet()
                    
                    val similar = artworkRepository.fetchSimilarArtists(
                        artistName = artist.name,
                        apiKey = lastFmApiKey,
                        localArtistNames = localArtistNamesLower,
                        maxResults = 2
                    )
                    discoveryDao.deleteAllByBecauseOfArtist(artist.name)
                    discoveryDao.insertAll(similar.map { item -> 
                        com.aeswox.arcmusic.db.entities.CachedDiscovery(
                            suggestedArtistName = item.suggestedArtistName,
                            becauseOfArtist = item.becauseOfArtist,
                            sharedGenre = item.sharedGenre,
                            imageUrl = item.imageUrl
                        )
                    })
                } catch (e: Exception) {
                    android.util.Log.w("MusicRepository", "Discovery fetch failed for ${artist.name}", e)
                }
            }
        }
    }

    suspend fun getDetailedDiscographyGaps(artist: Artist, forceRefresh: Boolean = false): Triple<List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>>? {
        val currentArtist = artistDao.getArtistById(artist.id).first() ?: artist
        if (!forceRefresh && currentArtist.hasScannedMissingContent) {
            // Check staleness — if cache is older than 7 days, re-fetch
            val oldestCachedAt = missingContentDao.getOldestCachedAt(currentArtist.name) ?: 0L
            val isStale = (System.currentTimeMillis() - oldestCachedAt) > CACHE_STALE_MS
            if (!isStale) {
                android.util.Log.d("MusicRepository", "Serving ${currentArtist.name} missing content from cache")
                return serveCachedMissingContent(currentArtist.name)
            }
            // Cache is stale — fall through to re-fetch
            android.util.Log.i("MusicRepository", "Cache stale for ${currentArtist.name}, re-fetching")
        }

        val deferred = inFlightGapsFetches.getOrPut(currentArtist.id) {
            backgroundScope.async {
                try {
                    val localAlbumObjects = albumDao.getAllAlbums().first().filter { a ->
                        ArtistUtils.splitArtists(a.artist).contains(currentArtist.name)
                    }
                    val localAlbums = localAlbumObjects.associate { it.title to it.trackCount }
                    val localAlbumCovers = localAlbumObjects.associate { it.title to it.artworkUri }

                    val localTracks = trackDao.getAllTracks().first().filter { t ->
                        ArtistUtils.splitArtists(t.artist).contains(currentArtist.name) ||
                        ArtistUtils.splitArtists(t.albumArtist).contains(currentArtist.name)
                    }.groupBy { it.album }.mapValues { entry -> entry.value.map { it.title } }
                    
                    val result = artworkRepository.getDetailedDiscographyGaps(currentArtist.name, localAlbums, localTracks, localAlbumCovers)
                    if (result != null) {
                        persistMissingContent(currentArtist.id, currentArtist.name, result)
                    }
                    result
                } finally {
                    inFlightGapsFetches.remove(currentArtist.id)
                }
            }
        }
        return deferred.await()
    }

    private suspend fun serveCachedMissingContent(artistName: String): Triple<List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>> {
        val cached = missingContentDao.getByArtistName(artistName)
        val adapter = com.squareup.moshi.Moshi.Builder().build().adapter<List<String>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java))
        val missingTracks = cached.filter { !it.isAlbum && !it.isSingle }.map { 
            val trackNames = it.missingTrackNamesJson?.let { json -> adapter.fromJson(json) } ?: emptyList()
            com.aeswox.arcmusic.MissingContentItem(it.title, it.artistName, it.isAlbum, it.isSingle, it.imageUrl, it.missingCount, trackNames)
        }
        val missingAlbums = cached.filter { it.isAlbum && !it.isSingle }.map { 
            com.aeswox.arcmusic.MissingContentItem(it.title, it.artistName, it.isAlbum, it.isSingle, it.imageUrl, it.missingCount)
        }
        val missingSingles = cached.filter { it.isSingle }.map { 
            val trackNames = it.missingTrackNamesJson?.let { json -> adapter.fromJson(json) } ?: emptyList()
            com.aeswox.arcmusic.MissingContentItem(it.title, it.artistName, it.isAlbum, it.isSingle, it.imageUrl, it.missingCount, trackNames)
        }
        return Triple(missingTracks, missingAlbums, missingSingles)
    }

    private suspend fun persistMissingContent(
        artistId: String,
        artistName: String,
        result: Triple<List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>>
    ) {
        val (missingTracks, missingAlbums, missingSingles) = result
        val adapter = com.squareup.moshi.Moshi.Builder().build().adapter<List<String>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java))
        val cachedItems = (missingTracks + missingAlbums + missingSingles).map { 
            com.aeswox.arcmusic.db.entities.CachedMissingContent(
                title = it.title,
                artistName = it.artistName,
                isAlbum = it.isAlbum,
                isSingle = it.isSingle,
                imageUrl = it.imageUrl,
                missingCount = it.missingCount,
                missingTrackNamesJson = if (it.missingTrackNames.isNotEmpty()) adapter.toJson(it.missingTrackNames) else null
            )
        }
        // Delete old entries first to prevent duplication on re-fetch
        missingContentDao.deleteAllByArtist(artistName)
        missingContentDao.insertAll(cachedItems)
        artistDao.updateHasScannedMissingContent(artistId, true)
    }

    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    fun getPlaylistsContainingTracks(trackIds: List<String>): Flow<List<String>> = playlistDao.getPlaylistsContainingTracks(trackIds)

    suspend fun deletePlaylists(playlistTitles: List<String>) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylists(playlistTitles)
    }

    suspend fun createPlaylist(name: String, description: String?, coverArtUri: String?, trackIds: List<String>) = withContext(Dispatchers.IO) {
        val playlistId = java.util.UUID.randomUUID().toString()
        val playlist = Playlist(
            id = playlistId,
            name = name,
            dateCreated = System.currentTimeMillis(),
            coverArtUri = coverArtUri,
            description = description
        )
        playlistDao.insertPlaylist(playlist)
        
        val playlistTracks = trackIds.mapIndexed { index, trackId ->
            PlaylistTrack(
                playlistId = playlistId,
                trackId = trackId,
                position = index
            )
        }
        playlistDao.insertPlaylistTracks(playlistTracks)
    }

    suspend fun addTracksToPlaylist(playlistId: String, trackIds: List<String>) = withContext(Dispatchers.IO) {
        val currentTracks = playlistDao.getTracksForPlaylistById(playlistId).first()
        val startPosition = currentTracks.size
        val playlistTracks = trackIds.mapIndexed { index, trackId ->
            PlaylistTrack(
                playlistId = playlistId,
                trackId = trackId,
                position = startPosition + index
            )
        }
        playlistDao.insertPlaylistTracks(playlistTracks)
    }

    suspend fun getEac3Track(): Track? {
        return trackDao.getEac3Track()
    }

    suspend fun searchArtistImages(artistName: String): List<String> {
        return artworkRepository.searchAllArtistImages(artistName)
    }

    suspend fun updateArtistPhoto(artistId: String, photoUri: String) {
        artistDao.updateArtistPhoto(artistId, photoUri)
    }

    suspend fun fetchMissingArtwork() = withContext(Dispatchers.IO) {
        // Fetch missing artist photos
        val artists = artistDao.getAllArtists().first()
        for (artist in artists) {
            if (artist.photoUri == null) {
                // We need a track to do the precision fetch
                val track = trackDao.getTracksByArtist(artist.name).first().firstOrNull()
                val photoUrl = artworkRepository.fetchBestArtistImage(artist.name, track?.title)
                if (photoUrl != null) {
                    artistDao.updateArtistPhoto(artist.id, photoUrl)
                }
            }
            if (artist.bioText == null) {
                val bio = artworkRepository.fetchArtistBio(artist.name)
                if (bio != null) {
                    artistDao.updateArtistBio(artist.id, bio)
                }
            }
        }

        // Fetch missing album artwork
        val albums = albumDao.getAllAlbums().first()
        for (album in albums) {
            if (album.artworkUri == null) {
                val coverUrl = artworkRepository.fetchAlbumCover(album.title, album.artist)
                if (coverUrl != null) {
                    albumDao.updateAlbumArtwork(album.id, coverUrl)
                    
                    val tracks = trackDao.getTracksByAlbum(album.title).first()
                    for (track in tracks) {
                        trackDao.updateTrackArtwork(track.id, coverUrl)
                    }
                }
            }
        }
        
        // Fetch missing track artwork for tracks that still don't have it (no album)
        val allTracks = trackDao.getAllTracks().first()
        for (track in allTracks) {
            if (track.artworkUri == null) {
                val artworkUrl = artworkRepository.fetchTrackArtwork(track.title, track.artist)
                if (artworkUrl != null) {
                    trackDao.updateTrackArtwork(track.id, artworkUrl)
                }
            }
        }
    }

    fun searchTracks(query: String): Flow<List<Track>> = trackDao.searchTracks(query)
    fun searchAlbums(query: String): Flow<List<Album>> = albumDao.searchAlbums(query)
    fun searchArtists(query: String): Flow<List<Artist>> = artistDao.searchArtists(query)
    fun searchPlaylists(query: String): Flow<List<Playlist>> = playlistDao.searchPlaylists(query)
    
    fun getAllGenres(): Flow<List<String>> = trackDao.getAllGenres()

    // Collection Health
    fun getTracksMissingArtwork(): Flow<List<Track>> = trackDao.getTracksMissingArtwork()
    fun getTracksMissingMetadata(): Flow<List<Track>> = trackDao.getTracksMissingMetadata()
    fun getLowQualityTracks(): Flow<List<Track>> = trackDao.getLowQualityTracks()
    fun getCorruptedTracks(): Flow<List<Track>> = trackDao.getCorruptedTracks()

    // Search History
    fun getRecentSearches(limit: Int = 4): Flow<List<SearchHistory>> = searchHistoryDao.getRecentSearches(limit)
    suspend fun addRecentSearch(query: String) = searchHistoryDao.insertSearch(SearchHistory(query = query))
    suspend fun deleteRecentSearch(query: String) = searchHistoryDao.deleteSearch(query)
    suspend fun clearRecentSearches() = searchHistoryDao.clearAll()

    // ---------------------------------------------------------------------------
    // Collection Growth data loading
    // ---------------------------------------------------------------------------

    /** Loads all four growth card types, using cached data where possible. */
    suspend fun loadCollectionGrowthData(
        lastFmApiKey: String?
    ): com.aeswox.arcmusic.CollectionGrowthData = withContext(Dispatchers.IO) {
        val favoritedArtists = artistDao.getAllArtists().first().filter { it.isFavorite }
        // Merge with top-listened artists so both sources feed the same pipeline.
        // Deduplication by id prevents double-fetching when an artist is both favorited and top-listened.
        val topListenedArtists = getTopListenedArtists(10)
        val qualifyingArtists = (favoritedArtists + topListenedArtists).distinctBy { it.id }
        android.util.Log.d("MusicRepository",
            "Growth pipeline: ${favoritedArtists.size} favorited, ${topListenedArtists.size} top-listened, " +
            "${qualifyingArtists.size} qualifying (after dedup)")

        val allAlbums = albumDao.getAllAlbums().first()
        val allTracks = trackDao.getAllTracks().first()
        val localAlbumTitles = allAlbums.map { it.title.lowercase() }.toSet()
        val localArtistNames = allTracks.map { it.artist.lowercase() }.toSet() +
            allAlbums.map { it.artist.lowercase() }.toSet()

        // Dismissed cards: load once for filtering
        val dismissed = dismissedCardDao.getAll().map {
            Triple(it.cardType, it.title.lowercase(), it.artistName.lowercase())
        }.toSet()

        fun isDismissed(cardType: String, title: String, artistName: String) =
            Triple(cardType, title.lowercase(), artistName.lowercase()) in dismissed

        val completeCollectionCards = mutableListOf<com.aeswox.arcmusic.GrowthCard.CompleteCollection>()
        val newReleaseCards = mutableListOf<com.aeswox.arcmusic.GrowthCard.NewRelease>()
        val discoveryCards = mutableListOf<com.aeswox.arcmusic.GrowthCard.Discovery>()
        val missingTracksCards = mutableListOf<com.aeswox.arcmusic.GrowthCard.MissingTracks>()

        for (artist in qualifyingArtists) {
            // --- Complete Collection + Missing Tracks: from CachedMissingContent ---
            val cached = missingContentDao.getByArtistName(artist.name)

            // Total official albums = local albums for this artist + missing album count
            val artistLocalAlbumCount = allAlbums.count { album ->
                ArtistUtils.splitArtists(album.artist).contains(artist.name)
            }
            val missingAlbumItems = cached.filter { it.isAlbum && !it.isSingle }
            val totalAlbumCount = artistLocalAlbumCount + missingAlbumItems.size

            // Complete Collection card: show if artist is "almost complete"
            // Threshold: missing ≤ 2 albums AND local owns at least 50% of discography
            if (missingAlbumItems.isNotEmpty() &&
                missingAlbumItems.size <= 2 &&
                artistLocalAlbumCount > 0 &&
                artistLocalAlbumCount.toFloat() / totalAlbumCount.toFloat() >= 0.5f
            ) {
                val firstMissing = missingAlbumItems.first()
                if (!isDismissed("complete_collection", firstMissing.title, artist.name)) {
                    completeCollectionCards.add(
                        com.aeswox.arcmusic.GrowthCard.CompleteCollection(
                            missingAlbumTitle = firstMissing.title,
                            artistName = artist.name,
                            ownedCount = artistLocalAlbumCount,
                            totalCount = totalAlbumCount,
                            imageUrl = firstMissing.imageUrl
                        )
                    )
                }
            }

            // Missing Tracks cards: partial albums (not entirely missing, not single)
            val partialAlbumItems = cached.filter { !it.isAlbum && !it.isSingle && it.missingCount > 0 }
            for (partial in partialAlbumItems.take(2)) { // max 2 per artist to avoid flooding
                if (!isDismissed("missing_tracks", partial.title, artist.name)) {
                    val artistAlbum = allAlbums.find {
                        it.title.lowercase() == partial.title.lowercase() ||
                        partial.title.lowercase().contains(it.title.lowercase())
                    }
                    val totalTracks = artistAlbum?.trackCount?.let { it + partial.missingCount } ?: partial.missingCount
                    val ownedTracks = totalTracks - partial.missingCount
                    missingTracksCards.add(
                        com.aeswox.arcmusic.GrowthCard.MissingTracks(
                            albumTitle = partial.title,
                            artistName = artist.name,
                            missingCount = partial.missingCount,
                            ownedCount = ownedTracks,
                            totalCount = totalTracks,
                            imageUrl = partial.imageUrl
                        )
                    )
                }
            }

            // --- New Releases: MusicBrainz release-groups (from cache) ---
            val cachedReleases = newReleaseDao.getByArtistName(artist.name)
            for (release in cachedReleases.take(2)) {
                if (!isDismissed("new_release", release.title, artist.name)) {
                    newReleaseCards.add(
                        com.aeswox.arcmusic.GrowthCard.NewRelease(
                            albumTitle = release.title,
                            artistName = release.artistName,
                            releaseType = release.releaseType,
                            releaseDateStr = release.releaseDateStr,
                            imageUrl = release.imageUrl
                        )
                    )
                }
            }
        }

        // --- Discovery: Last.fm artist.getSimilar (from cache) ---
        // Cap at 3 sources to limit the Discovery section size, drawing from qualifying artists.
        for (artist in qualifyingArtists.take(3)) {
            val cachedDiscoveries = discoveryDao.getByBecauseOfArtist(artist.name)
            for (item in cachedDiscoveries) {
                if (!isDismissed("discovery", item.suggestedArtistName, item.becauseOfArtist)) {
                    discoveryCards.add(
                        com.aeswox.arcmusic.GrowthCard.Discovery(
                            suggestedArtistName = item.suggestedArtistName,
                            becauseOfArtist = item.becauseOfArtist,
                            sharedGenre = item.sharedGenre,
                            imageUrl = item.imageUrl
                        )
                    )
                }
            }
        }

        com.aeswox.arcmusic.CollectionGrowthData(
            completeCollectionCards = completeCollectionCards,
            newReleaseCards = newReleaseCards,
            discoveryCards = discoveryCards,
            missingTracksCards = missingTracksCards,
            hasQualifyingArtists = qualifyingArtists.isNotEmpty()
        )
    }

    suspend fun dismissGrowthCard(
        cardType: String,
        title: String,
        artistName: String
    ) = withContext(Dispatchers.IO) {
        dismissedCardDao.dismiss(
            com.aeswox.arcmusic.db.entities.DismissedGrowthCard(
                cardType = cardType,
                title = title,
                artistName = artistName
            )
        )
    }

    private suspend fun fetchAndCacheNewReleases(
        artistName: String,
        localAlbumTitles: Set<String>,
        forceRefresh: Boolean
    ): List<com.aeswox.arcmusic.db.entities.CachedNewRelease> {
        if (!forceRefresh) return newReleaseDao.getByArtistName(artistName)
        return try {
            val mbid = artworkRepository.resolveArtistMbidPublic(artistName, localAlbumTitles)
            if (mbid != null) {
                kotlinx.coroutines.delay(1200)
                val newItems = artworkRepository.fetchNewReleases(
                    artistName = artistName,
                    mbid = mbid,
                    localTitles = localAlbumTitles
                )
                newReleaseDao.deleteAllByArtist(artistName)
                newReleaseDao.insertAll(newItems.map { item ->
                    com.aeswox.arcmusic.db.entities.CachedNewRelease(
                        title = item.title,
                        artistName = item.artistName,
                        releaseType = item.releaseType,
                        releaseDateStr = item.releaseDateStr,
                        imageUrl = item.imageUrl
                    )
                })
            }
            newReleaseDao.getByArtistName(artistName)
        } catch (e: Exception) {
            android.util.Log.w("MusicRepository", "New release fetch failed for $artistName", e)
            newReleaseDao.getByArtistName(artistName)
        }
    }
}



data class ScanResult(val trackCount: Int, val albumCount: Int, val artistCount: Int)
