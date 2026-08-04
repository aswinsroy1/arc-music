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
    private val mediaStoreScanner: MediaStoreScanner,
    private val artworkRepository: com.aeswox.arcmusic.data.network.ArtworkRepository
) {
    // Dedicated scope for background tasks — survives ViewModel but is still structured
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // 7 days in milliseconds — staleness policy for cached missing content
    private val CACHE_STALE_MS = 7L * 24 * 60 * 60 * 1000
    fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks()
    fun getAllAlbums(): Flow<List<Album>> = albumDao.getAllAlbums()
    fun getAllArtists(): Flow<List<Artist>> = artistDao.getAllArtists()
    fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>> = trackDao.getRecentlyPlayedTracks(limit)
    fun getRandomTracks(limit: Int): Flow<List<Track>> = trackDao.getRandomTracks(limit)
    fun getFullPlayHistory(): Flow<List<PlayHistory>> = playHistoryDao.getFullPlayHistory()

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

    suspend fun toggleArtistFavorite(artistId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        artistDao.updateArtistFavoriteStatus(artistId, isFavorite)
        if (isFavorite) {
            val artist = artistDao.getArtistById(artistId).first()
            if (artist != null) {
                val isStale = if (artist.hasScannedMissingContent) {
                    val oldestCachedAt = missingContentDao.getOldestCachedAt(artist.name) ?: 0L
                    (System.currentTimeMillis() - oldestCachedAt) > CACHE_STALE_MS
                } else true

                if (isStale) {
                    // Use backgroundScope instead of GlobalScope for structured concurrency
                    backgroundScope.launch {
                        try {
                            android.util.Log.i("MusicRepository", "Background scan started for ${artist.name}")
                            val localAlbums = albumDao.getAllAlbums().first().filter { a ->
                                ArtistUtils.splitArtists(a.artist).contains(artist.name)
                            }.associate { it.title to it.trackCount }
                            
                            val localTracks = trackDao.getAllTracks().first().filter { t ->
                                ArtistUtils.splitArtists(t.artist).contains(artist.name) ||
                                ArtistUtils.splitArtists(t.albumArtist).contains(artist.name)
                            }.groupBy { it.album }.mapValues { entry -> entry.value.map { it.title } }
                            
                            val result = artworkRepository.getDetailedDiscographyGaps(artist.name, localAlbums, localTracks)
                            if (result != null) {
                                persistMissingContent(artist.id, artist.name, result)
                                android.util.Log.i("MusicRepository", "Background scan complete for ${artist.name}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MusicRepository", "Background missing content scan failed for ${artist.name}", e)
                        }
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

    suspend fun getDetailedDiscographyGaps(artist: Artist, localAlbums: Map<String, Int>): Pair<List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>>? {
        if (artist.hasScannedMissingContent) {
            // Check staleness — if cache is older than 7 days, re-fetch
            val oldestCachedAt = missingContentDao.getOldestCachedAt(artist.name) ?: 0L
            val isStale = (System.currentTimeMillis() - oldestCachedAt) > CACHE_STALE_MS
            if (!isStale) {
                android.util.Log.d("MusicRepository", "Serving ${artist.name} missing content from cache")
                return serveCachedMissingContent(artist.name)
            }
            // Cache is stale — fall through to re-fetch
            android.util.Log.i("MusicRepository", "Cache stale for ${artist.name}, re-fetching")
        }

        val localTracks = trackDao.getAllTracks().first().filter { t ->
            ArtistUtils.splitArtists(t.artist).contains(artist.name) ||
            ArtistUtils.splitArtists(t.albumArtist).contains(artist.name)
        }.groupBy { it.album }.mapValues { entry -> entry.value.map { it.title } }
        
        val result = artworkRepository.getDetailedDiscographyGaps(artist.name, localAlbums, localTracks)
        if (result != null) {
            persistMissingContent(artist.id, artist.name, result)
        }
        return result
    }

    private suspend fun serveCachedMissingContent(artistName: String): Pair<List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>> {
        val cached = missingContentDao.getByArtistName(artistName)
        val adapter = com.squareup.moshi.Moshi.Builder().build().adapter<List<String>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java))
        val missingTracks = cached.filter { !it.isAlbum }.map { 
            val trackNames = it.missingTrackNamesJson?.let { json -> adapter.fromJson(json) } ?: emptyList()
            com.aeswox.arcmusic.MissingContentItem(it.title, it.artistName, it.isAlbum, it.imageUrl, it.missingCount, trackNames)
        }
        val missingAlbums = cached.filter { it.isAlbum }.map { 
            com.aeswox.arcmusic.MissingContentItem(it.title, it.artistName, it.isAlbum, it.imageUrl, it.missingCount)
        }
        return Pair(missingTracks, missingAlbums)
    }

    private suspend fun persistMissingContent(
        artistId: String,
        artistName: String,
        result: Pair<List<com.aeswox.arcmusic.MissingContentItem>, List<com.aeswox.arcmusic.MissingContentItem>>
    ) {
        val (missingTracks, missingAlbums) = result
        val adapter = com.squareup.moshi.Moshi.Builder().build().adapter<List<String>>(com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java))
        val cachedItems = (missingTracks + missingAlbums).map { 
            com.aeswox.arcmusic.db.entities.CachedMissingContent(
                title = it.title,
                artistName = it.artistName,
                isAlbum = it.isAlbum,
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
}


data class ScanResult(val trackCount: Int, val albumCount: Int, val artistCount: Int)
