package com.aeswox.arcmusic.data.network

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DeezerRepository @Inject constructor(
    private val deezerService: DeezerService
) {
    suspend fun fetchArtistImage(artistName: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = deezerService.searchArtist(artistName)
            val exactMatch = response.data.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
            val containsMatch = response.data.firstOrNull { it.name.contains(artistName, ignoreCase = true) || artistName.contains(it.name, ignoreCase = true) }
            // Fallback to first result if exact/contains match not found
            (exactMatch ?: containsMatch ?: response.data.firstOrNull())?.pictureXl
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Failed to fetch artist image for $artistName", e)
            null
        }
    }

    suspend fun searchArtistImages(artistName: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val response = deezerService.searchArtist(artistName)
            response.data.mapNotNull { it.pictureXl }.take(10)
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Failed to search artist images for $artistName", e)
            emptyList()
        }
    }

    suspend fun fetchArtistImageByTrack(artistName: String, trackTitle: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = "track:\"$trackTitle\" artist:\"$artistName\""
            val response = deezerService.searchTrack(query)
            val exactMatch = response.data.firstOrNull { it.title.equals(trackTitle, ignoreCase = true) }
            val containsMatch = response.data.firstOrNull { it.title.contains(trackTitle, ignoreCase = true) || trackTitle.contains(it.title, ignoreCase = true) }
            (exactMatch ?: containsMatch)?.artist?.pictureXl
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Failed to fetch artist image by track for $artistName - $trackTitle", e)
            null
        }
    }

    suspend fun fetchAlbumCover(albumName: String, artistName: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val query = if (artistName != null) "$albumName $artistName" else albumName
            val response = deezerService.searchAlbum(query)
            val exactMatch = response.data.firstOrNull { it.title.equals(albumName, ignoreCase = true) }
            val containsMatch = response.data.firstOrNull { it.title.contains(albumName, ignoreCase = true) || albumName.contains(it.title, ignoreCase = true) }
            (exactMatch ?: containsMatch)?.coverXl
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Failed to fetch album cover for $albumName", e)
            null
        }
    }

    suspend fun fetchTrackArtwork(trackTitle: String, artistName: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val query = if (artistName != null) "track:\"$trackTitle\" artist:\"$artistName\"" else trackTitle
            val response = deezerService.searchTrack(query)
            val exactMatch = response.data.firstOrNull { it.title.equals(trackTitle, ignoreCase = true) }
            val containsMatch = response.data.firstOrNull { it.title.contains(trackTitle, ignoreCase = true) || trackTitle.contains(it.title, ignoreCase = true) }
            (exactMatch ?: containsMatch)?.album?.coverXl
        } catch (e: Exception) {
            Log.e("DeezerRepository", "Failed to fetch track artwork for $trackTitle", e)
            null
        }
    }
}
