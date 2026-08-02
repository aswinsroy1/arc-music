package com.aeswox.arcmusic.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtworkRepository @Inject constructor(
    private val deezerRepository: DeezerRepository,
    private val lastFmService: LastFmService,
    private val theAudioDbService: TheAudioDbService,
    private val musicBrainzService: MusicBrainzService,
    private val settingsRepository: com.aeswox.arcmusic.data.SettingsRepository
) {
    suspend fun fetchBestArtistImage(artistName: String, trackTitle: String? = null): String? = withContext(Dispatchers.IO) {
        // 1. Try Deezer (by track + artist for precision)
        if (trackTitle != null) {
            val deezerTrackImg = deezerRepository.fetchArtistImageByTrack(artistName, trackTitle)
            if (deezerTrackImg != null) return@withContext deezerTrackImg
        }
        
        // 1.5. Try MusicBrainz MBID -> TheAudioDB (high precision, avoids name collisions)
        val mbImg = fetchArtistImageViaMusicBrainz(artistName)
        if (mbImg != null) return@withContext mbImg

        // 2. Try Deezer (by artist name)
        val deezerImg = deezerRepository.fetchArtistImage(artistName)
        if (deezerImg != null) return@withContext deezerImg

        // 3. Try TheAudioDB
        try {
            val tadbResponse = theAudioDbService.searchArtist(artist = artistName)
            val tadbImg = tadbResponse.artists?.firstOrNull()?.strArtistThumb
            if (!tadbImg.isNullOrBlank()) return@withContext tadbImg
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "TheAudioDB error for $artistName", e)
        }

        // 4. Try Last.fm
        try {
            val apiKey = settingsRepository.lastFmApiKey.firstOrNull()
            if (!apiKey.isNullOrBlank()) {
                val lfmResponse = lastFmService.getArtistInfo(artist = artistName, apiKey = apiKey)
                // Get the largest image ("extralarge" or "mega")
                val lfmImg = lfmResponse.artist?.image?.lastOrNull { it.text.isNotBlank() }?.text
                if (!lfmImg.isNullOrBlank() && !lfmImg.contains("2a96cbd8b46e442fc41c2b86b821562f")) { 
                    // Ignore default Last.fm star image
                    return@withContext lfmImg
                }
            }
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Last.fm error for $artistName", e)
        }
        
        // MusicBrainz is omitted for automated best image because it requires multi-step scraping
        // from Wikidata/Wikipedia relations which is too slow and complex for background tasks.
        
        null
    }
    
    suspend fun searchAllArtistImages(artistName: String): List<String> = withContext(Dispatchers.IO) {
        val allImages = mutableListOf<String>()
        
        // Deezer
        try {
            allImages.addAll(deezerRepository.searchArtistImages(artistName))
        } catch (e: Exception) {}
        
        // TheAudioDB
        try {
            val tadbResponse = theAudioDbService.searchArtist(artist = artistName)
            tadbResponse.artists?.forEach { artist ->
                artist.strArtistThumb?.let { if (it.isNotBlank()) allImages.add(it) }
            }
        } catch (e: Exception) {}

        // Last.fm
        try {
            val apiKey = settingsRepository.lastFmApiKey.firstOrNull()
            if (!apiKey.isNullOrBlank()) {
                val lfmResponse = lastFmService.getArtistInfo(artist = artistName, apiKey = apiKey)
                lfmResponse.artist?.image?.lastOrNull { it.text.isNotBlank() }?.text?.let { lfmImg ->
                    if (!lfmImg.contains("2a96cbd8b46e442fc41c2b86b821562f")) {
                        allImages.add(lfmImg)
                    }
                }
            }
        } catch (e: Exception) {}

        // MusicBrainz
        try {
            val mbResponse = musicBrainzService.searchArtist(query = artistName)
            // Just extract relation URLs as a fallback if they are image links (unlikely, but we try)
            mbResponse.artists?.forEach { artist ->
                artist.relations?.forEach { relation ->
                    val url = relation.url?.resource
                    if (url != null && (url.endsWith(".jpg") || url.endsWith(".png"))) {
                        allImages.add(url)
                    }
                }
            }
        } catch (e: Exception) {}
        
        allImages.distinct()
    }
    
    suspend fun fetchAlbumCover(albumTitle: String, artistName: String): String? {
        return deezerRepository.fetchAlbumCover(albumTitle, artistName)
    }
    
    suspend fun fetchTrackArtwork(trackTitle: String, artistName: String): String? {
        return deezerRepository.fetchTrackArtwork(trackTitle, artistName)
    }

    suspend fun fetchArtistBio(artistName: String): String? = withContext(Dispatchers.IO) {
        // Try Last.fm first (usually has better bios)
        try {
            val apiKey = settingsRepository.lastFmApiKey.firstOrNull()
            if (!apiKey.isNullOrBlank()) {
                val lfmResponse = lastFmService.getArtistInfo(artist = artistName, apiKey = apiKey)
                val bio = lfmResponse.artist?.bio?.content ?: lfmResponse.artist?.bio?.summary
                if (!bio.isNullOrBlank()) {
                    return@withContext bio.replace(Regex("<[^>]*>"), "").trim()
                }
            }
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Last.fm bio error for $artistName", e)
        }

        // Try TheAudioDB
        try {
            val tadbResponse = theAudioDbService.searchArtist(artist = artistName)
            val bio = tadbResponse.artists?.firstOrNull()?.strBiographyEN
            if (!bio.isNullOrBlank()) return@withContext bio.trim()
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "TheAudioDB bio error for $artistName", e)
        }

        null
    }

    private suspend fun fetchArtistImageViaMusicBrainz(artistName: String): String? {
        try {
            val mbResponse = musicBrainzService.searchArtist(query = artistName)
            val mbid = mbResponse.artists?.firstOrNull()?.id
            if (mbid != null) {
                val tadbResponse = theAudioDbService.searchArtistByMbid(mbid = mbid)
                val tadbImg = tadbResponse.artists?.firstOrNull()?.strArtistThumb
                if (!tadbImg.isNullOrBlank()) return tadbImg
            }
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "MusicBrainz MBID error for $artistName", e)
        }
        return null
    }
}
