package com.aeswox.arcmusic.data.network

import android.util.Log
import com.aeswox.arcmusic.MissingContentItem
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

    suspend fun fetchDiscographyGaps(artistName: String, localAlbums: Map<String, Int>): Pair<Int, Int>? {
        try {
            val mbResponse = musicBrainzService.searchArtist(query = artistName)
            val mbid = mbResponse.artists?.firstOrNull()?.id ?: return null

            // Respect MusicBrainz rate limiting (1 req/s) between the two endpoints
            kotlinx.coroutines.delay(1200)

            val query = "arid:$mbid AND status:official AND (primarytype:album OR primarytype:ep)"
            val releaseResponse = musicBrainzService.searchReleases(query = query, limit = 100)
            val releases = releaseResponse.releases ?: return null
            
            // 1. Filter out unwanted secondary types (live, compilations, soundtracks, etc.)
            val validReleases = releases.filter { release ->
                val rg = release.releaseGroup
                if (rg == null) return@filter false
                val secondaryTypes = rg.secondaryTypes ?: emptyList()
                val isUnwanted = secondaryTypes.any { type ->
                    type.equals("Live", ignoreCase = true) ||
                    type.equals("Compilation", ignoreCase = true) ||
                    type.equals("Interview", ignoreCase = true) ||
                    type.equals("Spokenword", ignoreCase = true) ||
                    type.equals("Audiobook", ignoreCase = true) ||
                    type.equals("Soundtrack", ignoreCase = true)
                }
                !isUnwanted
            }
            
            // 2. Group by release-group to treat all editions of one album as a single entity
            val groupedByRg = validReleases.groupBy { it.releaseGroup?.id ?: it.title }
            
            var missingAlbums = 0
            var missingTracks = 0
            
            groupedByRg.forEach { (_, rgReleases) ->
                // Use the canonical release group title (e.g. "Midnight Memories" instead of "Midnight Memories (Deluxe)")
                val rgTitle = rgReleases.first().releaseGroup?.title?.lowercase() ?: rgReleases.first().title.lowercase()
                
                // 3. Find the highest track count among all editions of this album
                val officialTrackCount = rgReleases.maxOfOrNull { r -> 
                    r.media?.sumOf { it.trackCount ?: 0 } ?: 0 
                } ?: 0

                val localMatch = localAlbums.entries.find { 
                    it.key.lowercase() == rgTitle || 
                    rgTitle.contains(it.key.lowercase()) || 
                    it.key.lowercase().contains(rgTitle) 
                }
                
                if (localMatch == null) {
                    missingAlbums++
                    missingTracks += officialTrackCount
                } else {
                    if (officialTrackCount > localMatch.value) {
                        missingTracks += (officialTrackCount - localMatch.value)
                    }
                }
            }
            return Pair(missingTracks, missingAlbums)
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Gap calculation error for $artistName", e)
            return null
        }
    }

    suspend fun getDetailedDiscographyGaps(artistName: String, localAlbums: Map<String, Int>): Pair<List<MissingContentItem>, List<MissingContentItem>>? {
        try {
            val mbResponse = musicBrainzService.searchArtist(query = artistName)
            val mbid = mbResponse.artists?.firstOrNull()?.id ?: return null
            
            // Respect MusicBrainz rate limiting (1 req/s) between the two endpoints
            kotlinx.coroutines.delay(1200)

            val query = "arid:$mbid AND status:official AND (primarytype:album OR primarytype:ep)"
            val releaseResponse = musicBrainzService.searchReleases(query = query, limit = 100)
            val releases = releaseResponse.releases ?: return null
            
            val validReleases = releases.filter { release ->
                val rg = release.releaseGroup
                if (rg == null) return@filter false
                val secondaryTypes = rg.secondaryTypes ?: emptyList()
                !secondaryTypes.any { type ->
                    type.equals("Live", ignoreCase = true) ||
                    type.equals("Compilation", ignoreCase = true) ||
                    type.equals("Interview", ignoreCase = true) ||
                    type.equals("Spokenword", ignoreCase = true) ||
                    type.equals("Audiobook", ignoreCase = true) ||
                    type.equals("Soundtrack", ignoreCase = true)
                }
            }
            
            val groupedByRg = validReleases.groupBy { it.releaseGroup?.id ?: it.title }
            
            val missingAlbums = mutableListOf<MissingContentItem>()
            val missingTracks = mutableListOf<MissingContentItem>()
            
            groupedByRg.forEach { (_, rgReleases) ->
                val rgTitle = rgReleases.first().releaseGroup?.title ?: rgReleases.first().title
                val officialTrackCount = rgReleases.maxOfOrNull { r -> 
                    r.media?.sumOf { it.trackCount ?: 0 } ?: 0 
                } ?: 0

                val localMatch = localAlbums.entries.find { 
                    it.key.lowercase() == rgTitle.lowercase() || 
                    rgTitle.lowercase().contains(it.key.lowercase()) || 
                    it.key.lowercase().contains(rgTitle.lowercase()) 
                }
                
                if (localMatch == null) {
                    val imageUrl = fetchAlbumCover(rgTitle, artistName)
                    missingAlbums.add(MissingContentItem(rgTitle, artistName, true, imageUrl))
                    missingTracks.add(MissingContentItem(rgTitle, artistName, false, imageUrl, missingCount = officialTrackCount))
                } else if (officialTrackCount > localMatch.value) {
                    val imageUrl = fetchAlbumCover(rgTitle, artistName)
                    missingTracks.add(MissingContentItem(rgTitle, artistName, false, imageUrl, missingCount = officialTrackCount - localMatch.value))
                }
            }
            return Pair(missingTracks, missingAlbums)
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Detailed gap calculation error for $artistName", e)
            return null
        }
    }
}
