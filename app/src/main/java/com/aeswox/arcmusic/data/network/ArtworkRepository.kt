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
    private val itunesService: ItunesService,
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
    
    suspend fun checkUrlExists(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.responseCode == 200 || connection.responseCode == 307
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchAlbumCover(albumTitle: String, artistName: String): String? {
        val cleanAlbum = albumTitle.replace(Regex("(?i)\\s*-\\s*EP$|\\s*\\(.*?Edition\\)$|\\s*\\[.*?\\]$"), "").trim()

        // 1. Try TheAudioDB (high precision, exact match on artist + album)
        try {
            val tadbResponse = theAudioDbService.searchAlbum(artist = artistName, album = cleanAlbum)
            val tadbUrl = tadbResponse.album?.firstOrNull()?.strAlbumThumb
            if (!tadbUrl.isNullOrBlank()) return tadbUrl
        } catch (e: Exception) {
            Log.w("ArtworkRepository", "TheAudioDB fetch failed for $cleanAlbum by $artistName", e)
        }

        // 2. Try iTunes (high quality, reliable, but search matching can be fuzzy)
        try {
            // We search for just the album name because iTunes sometimes suppresses exact matches when artist is included in term
            val response = itunesService.searchAlbum(term = cleanAlbum)
            val exactMatch = response.results?.firstOrNull {
                it.collectionName.contains(cleanAlbum, ignoreCase = true) && 
                (it.artistName.contains(artistName, ignoreCase = true) || artistName.contains(it.artistName, ignoreCase = true))
            }
            // Replace 100x100 with higher resolution 600x600 if it exists
            val itunesUrl = exactMatch?.artworkUrl100?.replace("100x100bb", "600x600bb")
            if (itunesUrl != null) return itunesUrl
        } catch (e: Exception) {
            Log.w("ArtworkRepository", "iTunes fetch failed for $cleanAlbum", e)
        }

        // 3. Try Deezer Album (often blocked by region but good if available)
        val direct = deezerRepository.fetchAlbumCover(cleanAlbum, artistName)
        if (direct != null) return direct
        
        // 4. Try Deezer Track Fallback
        return try {
            deezerRepository.fetchTrackArtwork(trackTitle = cleanAlbum, artistName = artistName)
        } catch (e: Exception) { null }
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

    /**
     * Returns the MBID for the given artist name, using local album AND track titles to disambiguate
     * between artists who share the same name (e.g. Lisa from BLACKPINK vs. Japanese Lisa).
     * Strategy: for each MusicBrainz candidate, fetch their releases and score by how many
     * local album titles OR local track titles overlap. The candidate with the highest score wins.
     */
    private suspend fun resolveArtistMbid(
        artistName: String,
        localAlbumTitles: Set<String> = emptySet(),
        localTrackTitles: Set<String> = emptySet()
    ): String? {
        try {
            // NEW: If we have local track titles, try a recording search to directly find the MBID.
            // This bypasses the issue where searchArtist doesn't include the correct artist in the top 5 (e.g., BLACKPINK Lisa).
            if (localTrackTitles.isNotEmpty()) {
                // Use up to 3 local tracks to build an OR query
                val trackQueries = localTrackTitles.take(3).joinToString(" OR ") { "recording:\"$it\"" }
                val query = "artist:\"$artistName\" AND ($trackQueries)"
                try {
                    val recResponse = musicBrainzService.searchRecording(query = query, limit = 10)
                    val artistIds = recResponse.recordings?.flatMap { r -> 
                        r.artistCredit?.mapNotNull { it.artist?.id } ?: emptyList() 
                    } ?: emptyList()
                    
                    if (artistIds.isNotEmpty()) {
                        // Find the artist ID that occurs most frequently in the top recording results
                        val mostFrequentId = artistIds.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
                        if (mostFrequentId != null) {
                            Log.i("ArtworkRepository", "Disambiguated '$artistName' via track search -> $mostFrequentId")
                            return mostFrequentId
                        }
                    }
                } catch (e: Exception) {
                    Log.w("ArtworkRepository", "Track-based MBID resolution failed for $artistName", e)
                }
                kotlinx.coroutines.delay(1200) // rate limit before next search
            }

            val mbResponse = musicBrainzService.searchArtist(query = artistName)
            val candidates = mbResponse.artists ?: return null
            if (candidates.isEmpty()) return null

            val hasLocalEvidence = localAlbumTitles.isNotEmpty() || localTrackTitles.isNotEmpty()
            if (hasLocalEvidence) {
                var bestMbid: String? = null
                var bestScore = -1

                for (candidate in candidates.take(5)) {
                    kotlinx.coroutines.delay(1200)
                    try {
                        val q = "arid:${candidate.id} AND status:official AND (primarytype:album OR primarytype:ep)"
                        val releases = musicBrainzService.searchReleases(query = q, limit = 25).releases ?: continue

                        // Album-title score: how many local album names match this candidate's release groups
                        val rgTitles = releases.mapNotNull { it.releaseGroup?.title ?: it.title }.map { it.lowercase() }.toSet()
                        val localAlbumLower = localAlbumTitles.map { it.lowercase() }.toSet()
                        val albumScore = rgTitles.count { rg -> localAlbumLower.any { local -> rg.contains(local) || local.contains(rg) } }

                        // Track-title score: for the best-matching release, fetch its track list and
                        // count how many local track titles appear in it
                        var trackScore = 0
                        if (localTrackTitles.isNotEmpty() && albumScore == 0) {
                            // Only do the expensive per-release lookup when album matching fails
                            val bestRelease = releases.maxByOrNull { r -> r.media?.sumOf { it.trackCount ?: 0 } ?: 0 }
                            if (bestRelease != null) {
                                kotlinx.coroutines.delay(1200)
                                try {
                                    val fullRelease = musicBrainzService.getReleaseById(bestRelease.id)
                                    val releaseTracks = fullRelease.media
                                        ?.flatMap { it.tracks ?: emptyList() }
                                        ?.map { it.title.lowercase() } ?: emptyList()
                                    val localTrackLower = localTrackTitles.map { it.lowercase() }.toSet()
                                    trackScore = releaseTracks.count { rt ->
                                        localTrackLower.any { lt -> rt.contains(lt) || lt.contains(rt) }
                                    }
                                } catch (e: Exception) {
                                    Log.w("ArtworkRepository", "Track lookup failed for ${bestRelease.id}", e)
                                }
                            }
                        }

                        val totalScore = albumScore * 2 + trackScore // album matches worth more
                        Log.d("ArtworkRepository", "Candidate ${candidate.name} (${candidate.id}): album=$albumScore track=$trackScore total=$totalScore")
                        if (totalScore > bestScore) {
                            bestScore = totalScore
                            bestMbid = candidate.id
                        }
                        if (bestScore >= 2) break // confident enough, stop early
                    } catch (e: Exception) {
                        Log.w("ArtworkRepository", "Failed to fetch releases for candidate ${candidate.id}", e)
                    }
                }

                if (bestMbid != null && bestScore >= 1) {
                    Log.i("ArtworkRepository", "Disambiguated '$artistName' -> $bestMbid (score=$bestScore)")
                    return bestMbid
                }
            }

            // Fallback: just return the top result
            Log.i("ArtworkRepository", "No disambiguation evidence for '$artistName', using top result")
            return candidates.firstOrNull()?.id
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "MBID resolution error for $artistName", e)
            return null
        }
    }

    private suspend fun fetchArtistImageViaMusicBrainz(artistName: String, localAlbumTitles: Set<String> = emptySet(), localTrackTitles: Set<String> = emptySet()): String? {
        try {
            val mbid = resolveArtistMbid(artistName, localAlbumTitles, localTrackTitles) ?: return null
            val tadbResponse = theAudioDbService.searchArtistByMbid(mbid = mbid)
            val tadbImg = tadbResponse.artists?.firstOrNull()?.strArtistThumb
            if (!tadbImg.isNullOrBlank()) return tadbImg
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

    suspend fun getDetailedDiscographyGaps(
        artistName: String,
        localAlbums: Map<String, Int>,
        localTracks: Map<String, List<String>> = emptyMap(),
        localAlbumCovers: Map<String, String?> = emptyMap() // album title -> local artworkUri
    ): Pair<List<MissingContentItem>, List<MissingContentItem>>? {
        try {
            // Disambiguate using both album titles AND individual track titles for artists like Lisa
            val localAlbumTitles = localAlbums.keys.toSet()
            val localTrackTitles = localTracks.values.flatten().toSet()
            val mbid = resolveArtistMbid(artistName, localAlbumTitles, localTrackTitles) ?: return null
            Log.i("ArtworkRepository", "Resolved MBID for $artistName: $mbid")
            
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
                    // Entirely missing album — fetch cover from Cover Art Archive (bypasses Deezer region blocks)
                    val rgId = rgReleases.first().releaseGroup?.id
                    val caaUrl = if (rgId != null) "https://coverartarchive.org/release-group/$rgId/front" else null
                    val imageUrl = if (caaUrl != null && checkUrlExists(caaUrl)) {
                        caaUrl
                    } else {
                        fetchAlbumCover(rgTitle, artistName)
                    }
                    missingAlbums.add(MissingContentItem(rgTitle, artistName, true, imageUrl, missingCount = officialTrackCount))
                } else if (officialTrackCount > localMatch.value) {
                    // Partial album — prefer local artwork; only go online if none available locally
                    val localArtwork: String? = localAlbumCovers.entries.find { (albumTitle, _) ->
                        albumTitle.lowercase() == rgTitle.lowercase() ||
                        rgTitle.lowercase().contains(albumTitle.lowercase()) ||
                        albumTitle.lowercase().contains(rgTitle.lowercase())
                    }?.value

                    val bestRelease = rgReleases.maxByOrNull { r -> r.media?.sumOf { it.trackCount ?: 0 } ?: 0 }
                    val officialTracks: List<String> = if (bestRelease != null) {
                        kotlinx.coroutines.delay(1200) // rate limit
                        try {
                            val fullRelease = musicBrainzService.getReleaseById(bestRelease.id)
                            fullRelease.media?.flatMap { it.tracks ?: emptyList() }?.map { it.title } ?: emptyList()
                        } catch (e: Exception) {
                            Log.w("ArtworkRepository", "Failed to fetch tracks for release ${bestRelease.id}", e)
                            emptyList()
                        }
                    } else emptyList()

                    val myLocalTracksForAlbum = localTracks.entries.find { 
                        it.key.lowercase() == rgTitle.lowercase() || 
                        rgTitle.lowercase().contains(it.key.lowercase()) || 
                        it.key.lowercase().contains(rgTitle.lowercase()) 
                    }?.value?.map { it.lowercase() } ?: emptyList()
                    
                    val actualMissingTrackNames = if (officialTracks.isNotEmpty()) {
                        officialTracks.filter { officialTitle ->
                            !myLocalTracksForAlbum.any { localTitle ->
                                localTitle.contains(officialTitle.lowercase()) || officialTitle.lowercase().contains(localTitle)
                            }
                        }
                    } else emptyList()
                    
                    val finalCount = if (actualMissingTrackNames.isNotEmpty()) actualMissingTrackNames.size else (officialTrackCount - localMatch.value)
                    // Use local artwork if available; fallback to Cover Art Archive, then iTunes/Deezer
                    val rgId = rgReleases.first().releaseGroup?.id
                    val caaUrl = if (rgId != null) "https://coverartarchive.org/release-group/$rgId/front" else null
                    
                    val imageUrl = localArtwork ?: if (caaUrl != null && checkUrlExists(caaUrl)) {
                        caaUrl
                    } else {
                        fetchAlbumCover(rgTitle, artistName)
                    }
                    missingTracks.add(MissingContentItem(rgTitle, artistName, false, imageUrl, missingCount = finalCount, missingTrackNames = actualMissingTrackNames))
                }
            }
            return Pair(missingTracks, missingAlbums)
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Detailed gap calculation error for $artistName", e)
            return null
        }
    }
}
