package com.aeswox.arcmusic.data.network

import android.util.Log
import com.aeswox.arcmusic.MissingContentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ArtworkRepository @Inject constructor(
    private val deezerRepository: DeezerRepository,
    private val lastFmService: LastFmService,
    private val theAudioDbService: TheAudioDbService,
    private val musicBrainzService: MusicBrainzService,
    private val itunesService: ItunesService,
    private val fanartTvService: FanartTvService,
    private val wikipediaService: WikipediaService,
    private val settingsRepository: com.aeswox.arcmusic.data.SettingsRepository
) {
    private val mbMutex = Mutex()
    private var lastMbRequestTime = 0L

    private suspend fun <T> withMusicBrainzRateLimit(block: suspend () -> T): T {
        return mbMutex.withLock {
            val now = System.currentTimeMillis()
            val timeSinceLast = now - lastMbRequestTime
            if (timeSinceLast < 1200) {
                kotlinx.coroutines.delay(1200 - timeSinceLast)
            }
            val result = block()
            lastMbRequestTime = System.currentTimeMillis()
            result
        }
    }

    suspend fun fetchBestArtistImage(artistName: String, trackTitle: String? = null): String? = withContext(Dispatchers.IO) {
        // 1. Try Deezer (by track + artist for precision, mimicking PixelPlayer + Disambiguation)
        if (trackTitle != null) {
            val deezerTrackImg = deezerRepository.fetchArtistImageByTrack(artistName, trackTitle)
            if (deezerTrackImg != null) return@withContext deezerTrackImg
        }

        // 2. Try Deezer (by artist name directly, highly reliable fallback)
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

        // 4. Try Last.fm (often returns default star image, but we filter it out)
        try {
            val apiKey = settingsRepository.lastFmApiKey.firstOrNull()
            if (!apiKey.isNullOrBlank()) {
                val lfmResponse = lastFmService.getArtistInfo(artist = artistName, apiKey = apiKey)
                // Get the largest image ("extralarge" or "mega")
                val lfmImg = lfmResponse.artist?.image?.lastOrNull { it.text?.isNotBlank() == true }?.text
                if (!lfmImg.isNullOrBlank() && !lfmImg.contains("2a96cbd8b46e442fc41c2b86b821562f")) { 
                    // Ignore default Last.fm star image
                    return@withContext lfmImg
                }
            }
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Last.fm error for $artistName", e)
        }
        
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
                lfmResponse.artist?.image?.lastOrNull { it.text?.isNotBlank() == true }?.text?.let { lfmImg ->
                    if (!lfmImg.contains("2a96cbd8b46e442fc41c2b86b821562f")) {
                        allImages.add(lfmImg)
                    }
                }
            }
        } catch (e: Exception) {}

        // MusicBrainz
        try {
            val mbResponse = withMusicBrainzRateLimit { musicBrainzService.searchArtist(query = artistName) }
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
    
    private fun isFuzzyMatch(official: String, local: String): Boolean {
        val off = official.trim().lowercase()
        val loc = local.trim().lowercase()
        
        // Ignore blank or placeholder metadata
        if (loc.isBlank() || loc == "unknown" || loc == "unknown album" || loc == "null") return false
        if (off.isBlank() || off == "unknown" || off == "unknown album" || off == "null") return false
        
        if (off == loc) return true
        
        // Only allow .contains() if the string is reasonably long (e.g. > 3 chars)
        // to prevent a 1-character album name from matching everything.
        if (loc.length > 3 && off.contains(loc)) return true
        if (off.length > 3 && loc.contains(off)) return true
        
        return false
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
                (it.collectionName?.contains(cleanAlbum, ignoreCase = true) == true) && 
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
        // 1. Try Last.fm (primary choice based on user preference)
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

        // 2. Try TheAudioDB (fast public fallback)
        try {
            val tadbResponse = theAudioDbService.searchArtist(artist = artistName)
            val bio = tadbResponse.artists?.firstOrNull()?.strBiographyEN
            if (!bio.isNullOrBlank()) return@withContext bio.trim()
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "TheAudioDB bio error for $artistName", e)
        }

        // 3. Try Wikipedia (fast title guessing only, no MusicBrainz relation lookup)
        try {
            val title = java.net.URLEncoder.encode(artistName.replace(" ", "_"), "UTF-8")
            val response = wikipediaService.getSummary("https://en.wikipedia.org/api/rest_v1/page/summary/$title")
            val lowerExtract = response.extract?.lowercase() ?: ""
            if (lowerExtract.contains("musician") || lowerExtract.contains("singer") || lowerExtract.contains("band") || lowerExtract.contains("rapper") || lowerExtract.contains("group") || lowerExtract.contains("artist")) {
                if (!response.extract.isNullOrBlank()) {
                    return@withContext response.extract.trim()
                }
            }
        } catch (e: Exception) {
            Log.w("ArtworkRepository", "Wikipedia bio error for $artistName", e)
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
                    val recResponse = withMusicBrainzRateLimit { musicBrainzService.searchRecording(query = query, limit = 10) }
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

            val mbResponse = withMusicBrainzRateLimit { musicBrainzService.searchArtist(query = artistName) }
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
                        val releases = withMusicBrainzRateLimit { musicBrainzService.searchReleases(query = q, limit = 25) }.releases ?: continue

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
                                    val fullRelease = withMusicBrainzRateLimit { musicBrainzService.getReleaseById(bestRelease.id) }
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

    /** Public wrapper so MusicRepository can resolve MBIDs for new-release fetching. */
    suspend fun resolveArtistMbidPublic(
        artistName: String,
        localAlbumTitles: Set<String> = emptySet()
    ): String? = resolveArtistMbid(artistName, localAlbumTitles)

    private suspend fun fetchArtistImageViaMusicBrainz(artistName: String, localAlbumTitles: Set<String> = emptySet(), localTrackTitles: Set<String> = emptySet()): String? {

        try {
            val mbid = resolveArtistMbid(artistName, localAlbumTitles, localTrackTitles) ?: return null
            
            // 1.5a Try Fanart.tv
            try {
                val fanartKey = settingsRepository.fanartTvApiKey.firstOrNull()
                if (!fanartKey.isNullOrBlank()) {
                    val response = fanartTvService.getArtistImages(mbid, fanartKey)
                    val img = response.artistthumb?.firstOrNull()?.url
                    if (!img.isNullOrBlank()) return img
                }
            } catch (e: Exception) {
                Log.w("ArtworkRepository", "Fanart.tv fetch failed for $artistName", e)
            }

            // 1.5b Fallback to TheAudioDB
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
            val mbResponse = withMusicBrainzRateLimit { musicBrainzService.searchArtist(query = artistName) }
            val mbid = mbResponse.artists?.firstOrNull()?.id ?: return null

            // Respect MusicBrainz rate limiting (1 req/s) between the two endpoints
            kotlinx.coroutines.delay(1200)

            val query = "arid:$mbid AND status:official AND (primarytype:album OR primarytype:ep)"
            val releaseResponse = withMusicBrainzRateLimit { musicBrainzService.searchReleases(query = query, limit = 100) }
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
    ): Triple<List<MissingContentItem>, List<MissingContentItem>, List<MissingContentItem>>? {
        try {
            // Disambiguate using both album titles AND individual track titles for artists like Lisa
            val localAlbumTitles = localAlbums.keys.toSet()
            val localTrackTitles = localTracks.values.flatten().toSet()
            val mbid = resolveArtistMbid(artistName, localAlbumTitles, localTrackTitles) ?: return null
            Log.i("ArtworkRepository", "Resolved MBID for $artistName: $mbid")
            
            kotlinx.coroutines.delay(1200)

            val query = "arid:$mbid AND status:official AND (primarytype:album OR primarytype:ep OR primarytype:single)"
            val releaseResponse = withMusicBrainzRateLimit { musicBrainzService.searchReleases(query = query, limit = 100) }
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
            val missingSingles = mutableListOf<MissingContentItem>()
            
            groupedByRg.forEach { (_, rgReleases) ->
                val rgTitle = rgReleases.first().releaseGroup?.title ?: rgReleases.first().title
                val primaryType = rgReleases.first().releaseGroup?.primaryType ?: ""
                val isSingle = primaryType.equals("Single", ignoreCase = true)
                val officialTrackCounts = rgReleases.map { r -> 
                    r.media?.sumOf { it.trackCount ?: 0 } ?: 0 
                }
                val maxOfficialTrackCount = officialTrackCounts.maxOrNull() ?: 0

                val localMatch = localAlbums.entries.find { isFuzzyMatch(rgTitle, it.key) }
                val matchesAnyOfficialEdition = localMatch != null && officialTrackCounts.contains(localMatch.value)
                
                if (localMatch == null) {
                    // Entirely missing album — fetch cover from Cover Art Archive (bypasses Deezer region blocks)
                    val rgId = rgReleases.first().releaseGroup?.id
                    val caaUrl = if (rgId != null) "https://coverartarchive.org/release-group/$rgId/front" else null
                    val imageUrl = if (caaUrl != null && checkUrlExists(caaUrl)) {
                        caaUrl
                    } else {
                        fetchAlbumCover(rgTitle, artistName)
                    }
                    val newItem = MissingContentItem(rgTitle, artistName, true, isSingle, imageUrl, missingCount = maxOfficialTrackCount)
                    if (isSingle) {
                        missingSingles.add(newItem)
                    } else {
                        missingAlbums.add(newItem)
                    }
                } else if (!matchesAnyOfficialEdition && maxOfficialTrackCount > localMatch.value) {
                    // Partial album — prefer local artwork; only go online if none available locally
                    val localArtwork: String? = localAlbumCovers.entries.find { (albumTitle, _) ->
                        isFuzzyMatch(rgTitle, albumTitle)
                    }?.value

                    val bestRelease = rgReleases.maxByOrNull { r -> r.media?.sumOf { it.trackCount ?: 0 } ?: 0 }
                    val rawOfficialTracks: List<String> = if (bestRelease != null) {
                        kotlinx.coroutines.delay(1200) // rate limit
                        try {
                            val fullRelease = withMusicBrainzRateLimit { musicBrainzService.getReleaseById(bestRelease.id) }
                            fullRelease.media?.flatMap { it.tracks ?: emptyList() }?.map { it.title } ?: emptyList()
                        } catch (e: Exception) {
                            Log.w("ArtworkRepository", "Failed to fetch tracks for release ${bestRelease.id}", e)
                            emptyList()
                        }
                    } else emptyList()

                    val officialTracks = rawOfficialTracks.filter { title ->
                        val lower = title.lowercase()
                        !lower.contains("instrumental") && !lower.contains("inst.") && !lower.contains("karaoke") && !lower.contains("acapella")
                    }

                    val myLocalTracksForAlbum = localTracks.entries.find { 
                        isFuzzyMatch(rgTitle, it.key)
                    }?.value?.map { it.lowercase() } ?: emptyList()
                    
                    val actualMissingTrackNames = if (rawOfficialTracks.isNotEmpty()) {
                        officialTracks.filter { officialTitle ->
                            !myLocalTracksForAlbum.any { localTitle ->
                                isFuzzyMatch(officialTitle, localTitle)
                            }
                        }
                    } else emptyList()
                    
                    if (rawOfficialTracks.isNotEmpty() && actualMissingTrackNames.isEmpty()) {
                        return@forEach // User has all the non-instrumental tracks for this release
                    }
                    
                    val finalCount = if (rawOfficialTracks.isNotEmpty()) actualMissingTrackNames.size else (maxOfficialTrackCount - localMatch.value)
                    
                    if (finalCount <= 0) return@forEach
                    // Use local artwork if available; fallback to Cover Art Archive, then iTunes/Deezer
                    val rgId = rgReleases.first().releaseGroup?.id
                    val caaUrl = if (rgId != null) "https://coverartarchive.org/release-group/$rgId/front" else null
                    
                    val imageUrl = localArtwork ?: if (caaUrl != null && checkUrlExists(caaUrl)) {
                        caaUrl
                    } else {
                        fetchAlbumCover(rgTitle, artistName)
                    }
                    val newItem = MissingContentItem(rgTitle, artistName, false, isSingle, imageUrl, missingCount = finalCount, missingTrackNames = actualMissingTrackNames)
                    if (isSingle) {
                        missingSingles.add(newItem)
                    } else {
                        missingTracks.add(newItem)
                    }
                }
            }
            return Triple(missingTracks, missingAlbums, missingSingles)
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Detailed gap calculation error for $artistName", e)
            return null
        }
    }

    // ---------------------------------------------------------------------------
    // New Release detection: MusicBrainz release-groups within the last 90 days
    // that are not yet in the local library.
    // ---------------------------------------------------------------------------

    /**
     * Returns release groups published within [windowDays] days for the given [mbid]
     * that don't appear in [localTitles]. Respects MusicBrainz rate limits.
     */
    suspend fun fetchNewReleases(
        artistName: String,
        mbid: String,
        localTitles: Set<String>,
        windowDays: Int = 90
    ): List<NewReleaseItem> = withContext(Dispatchers.IO) {
        try {
            val cutoffMs = System.currentTimeMillis() - windowDays.toLong() * 24 * 3600 * 1000
            // Use ISO date string YYYY-MM-DD for cutoff comparison
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = cutoffMs }
            val cutoffYear = cal.get(java.util.Calendar.YEAR)
            val cutoffMonth = cal.get(java.util.Calendar.MONTH) + 1
            val cutoffDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val cutoffDateStr = "%04d-%02d-%02d".format(cutoffYear, cutoffMonth, cutoffDay)

            val query = "arid:$mbid AND status:official"
            val response = withMusicBrainzRateLimit { musicBrainzService.searchReleaseGroups(query = query, limit = 50) }
            val groups = response.releaseGroups ?: return@withContext emptyList()

            val results = mutableListOf<NewReleaseItem>()

            for (rg in groups) {
                val dateStr = rg.firstReleaseDate ?: continue
                // Only consider entries with at least a year (length >= 4)
                if (dateStr.length < 4) continue

                // Skip unwanted secondary types
                val secondary = rg.secondaryTypes ?: emptyList()
                if (secondary.any { it.equals("Live", ignoreCase = true) ||
                        it.equals("Compilation", ignoreCase = true) ||
                        it.equals("Interview", ignoreCase = true) ||
                        it.equals("Spokenword", ignoreCase = true) ||
                        it.equals("Audiobook", ignoreCase = true) }) continue

                // Date comparison: pad short dates for string comparison
                val paddedDate = when (dateStr.length) {
                    4 -> "$dateStr-01-01"     // year only
                    7 -> "$dateStr-01"         // year-month
                    else -> dateStr
                }
                if (paddedDate < cutoffDateStr) continue

                // Skip if already in local library (case-insensitive title match)
                val rgTitleLower = rg.title.lowercase()
                if (localTitles.any { local -> local == rgTitleLower || local.contains(rgTitleLower) || rgTitleLower.contains(local) }) continue

                // Use Cover Art Archive URL directly — if it doesn't load, Coil will show placeholder
                val imageUrl = "https://coverartarchive.org/release-group/${rg.id}/front"

                val releaseType = when {
                    rg.primaryType?.equals("Single", ignoreCase = true) == true -> "Single"
                    rg.primaryType?.equals("EP", ignoreCase = true) == true -> "EP"
                    else -> "Album"
                }

                results.add(NewReleaseItem(
                    title = rg.title,
                    artistName = artistName,
                    releaseType = releaseType,
                    releaseDateStr = dateStr,
                    imageUrl = imageUrl
                ))
            }
            results
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "New release fetch error for $artistName (mbid=$mbid)", e)
            emptyList()
        }
    }

    // ---------------------------------------------------------------------------
    // Discovery: Last.fm artist.getSimilar — gated on API key presence
    // ---------------------------------------------------------------------------

    /**
     * Returns up to [maxResults] similar artists for [artistName] that are not already
     * in [localArtistNames], with a shared genre tag if available.
     * Returns empty list immediately if [apiKey] is null or blank.
     */
    suspend fun fetchSimilarArtists(
        artistName: String,
        apiKey: String,
        localArtistNames: Set<String>,
        maxResults: Int = 3
    ): List<DiscoveryItem> = withContext(Dispatchers.IO) {
        try {
            val similarResponse = lastFmService.getArtistSimilar(
                artist = artistName,
                apiKey = apiKey,
                limit = 20
            )
            val similar = similarResponse.similarartists?.artist ?: return@withContext emptyList()

            val results = mutableListOf<DiscoveryItem>()
            for (candidate in similar) {
                if (results.size >= maxResults) break
                // Skip if the similar artist is already in local library
                val nameLower = candidate.name?.lowercase() ?: ""
                if (localArtistNames.any { local -> local.equals(nameLower, ignoreCase = true) }) continue

                // Fetch top tags for the similar artist to get a shared genre
                var sharedTag: String? = null
                try {
                    kotlinx.coroutines.delay(300) // mild throttle for Last.fm (no strict rate limit, but be polite)
                    val tagsResponse = lastFmService.getArtistTopTags(
                        artist = candidate.name ?: "",
                        apiKey = apiKey
                    )
                    sharedTag = tagsResponse.toptags?.tag?.firstOrNull()?.name
                } catch (e: Exception) {
                    Log.w("ArtworkRepository", "Top tags fetch failed for ${candidate.name}", e)
                }

                // Use Last.fm image if available (extralarge or largest non-blank)
                var imageUrl = candidate.image?.lastOrNull { it.text?.isNotBlank() == true &&
                    it.text.contains("2a96cbd8b46e442fc41c2b86b821562f") == false }?.text

                // Task 0 fix: Last.fm getSimilar images are almost always placeholders.
                // Fall back to the same multi-source artwork chain used for library artists.
                if (imageUrl.isNullOrBlank()) {
                    try {
                        kotlinx.coroutines.delay(300)
                        imageUrl = fetchBestArtistImage(candidate.name ?: "")
                    } catch (e: Exception) {
                        Log.w("ArtworkRepository", "Fallback image fetch failed for ${candidate.name}", e)
                    }
                }

                results.add(DiscoveryItem(
                    suggestedArtistName = candidate.name ?: "",
                    becauseOfArtist = artistName,
                    sharedGenre = sharedTag,
                    imageUrl = imageUrl
                ))
            }
            results
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Similar artists fetch error for $artistName", e)
            emptyList()
        }
    }

    // ---------------------------------------------------------------------------
    // New Songs detection: MusicBrainz recording search for individual tracks
    // released within the last 90 days that are not in the local library.
    // ---------------------------------------------------------------------------

    /**
     * Returns recordings by [mbid] with [firstReleaseDate] within [windowDays] that don't appear
     * in [localTrackTitles]. Respects MusicBrainz rate limits (caller must insert 1.2s delay before).
     */
    suspend fun fetchNewSongs(
        artistName: String,
        mbid: String,
        localTrackTitles: Set<String>,
        windowDays: Int = 90
    ): List<NewSongItem> = withContext(Dispatchers.IO) {
        try {
            val cutoffMs = System.currentTimeMillis() - windowDays.toLong() * 24 * 3600 * 1000
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = cutoffMs }
            val cutoffDateStr = "%04d-%02d-%02d".format(
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            )

            // MusicBrainz recording search: recordings by this artist with a recent first release date.
            // Use limit=50 so we don't miss tracks from prolific artists.
            val query = "arid:$mbid AND firstreleasedate:[${cutoffDateStr} TO *]"
            val response = withMusicBrainzRateLimit { musicBrainzService.searchRecording(query = query, limit = 50) }
            val recordings = response.recordings ?: return@withContext emptyList()

            val results = mutableListOf<NewSongItem>()
            for (rec in recordings) {
                val dateStr = rec.firstReleaseDate ?: continue
                if (dateStr.length < 4) continue

                val paddedDate = when (dateStr.length) {
                    4 -> "$dateStr-01-01"
                    7 -> "$dateStr-01"
                    else -> dateStr
                }
                if (paddedDate < cutoffDateStr) continue

                // Skip if already in local library
                val titleLower = rec.title.lowercase()
                if (localTrackTitles.any { lt -> lt == titleLower || lt.contains(titleLower) || titleLower.contains(lt) }) continue

                // Filter instrumentals / karaoke
                if (titleLower.contains("instrumental") || titleLower.contains("inst.") ||
                    titleLower.contains("karaoke") || titleLower.contains("acapella")) continue

                // Artwork: Cover Art Archive doesn't index individual recordings — use null;
                // the card will show a placeholder. A future enhancement could look up the
                // release the recording appears on.
                results.add(NewSongItem(
                    trackTitle = rec.title,
                    artistName = artistName,
                    mbid = rec.id,
                    releaseDateStr = dateStr,
                    imageUrl = null
                ))

                if (results.size >= 5) break // cap per artist to avoid flooding the section
            }
            results
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "New songs fetch error for $artistName (mbid=$mbid)", e)
            emptyList()
        }
    }

    // ---------------------------------------------------------------------------
    // Trending: Last.fm chart.getTopTracks, biased toward user's top genres.
    // Gated on Last.fm API key — returns empty list immediately if no key.
    // ---------------------------------------------------------------------------

    /**
     * Fetches up to [targetCount] globally-trending tracks, ranked by how well each
     * track's artist genre overlaps with [userGenres]. Excludes tracks already in
     * [localTrackTitles] or from artists already in [localArtistNames].
     */
    suspend fun fetchTrendingTracks(
        apiKey: String,
        localTrackTitles: Set<String>,
        localArtistNames: Set<String>,
        userGenres: List<String>,
        targetCount: Int = 10
    ): List<TrendingItem> = withContext(Dispatchers.IO) {
        try {
            val response = lastFmService.getChartTopTracks(apiKey = apiKey, limit = 50)
            val tracks = response.tracks?.track ?: return@withContext emptyList()

            val userGenresLower = userGenres.map { it.lowercase() }
            data class ScoredTrack(val track: LastFmChartTrack, val score: Int, val matchedGenre: String?, val imageUrl: String?)
            val scored = mutableListOf<ScoredTrack>()

            for (track in tracks) {
                val artistName = track.artist?.name ?: continue
                val titleLower = track.name?.lowercase() ?: ""
                val artistLower = artistName.lowercase()

                // Exclude tracks already in local library
                if (localTrackTitles.any { lt -> lt == titleLower || lt.contains(titleLower) || titleLower.contains(lt) }) continue
                // Exclude artists already in library (we know them; this is discovery)
                if (localArtistNames.any { la -> la.equals(artistLower, ignoreCase = true) }) continue

                // Fetch genre tags for this artist to compute genre-affinity score
                var score = 0
                var matchedGenre: String? = null
                if (userGenresLower.isNotEmpty()) {
                    try {
                        kotlinx.coroutines.delay(300)
                        val tagsResponse = lastFmService.getArtistTopTags(artist = artistName, apiKey = apiKey)
                        val artistTags = tagsResponse.toptags?.tag?.mapNotNull { it.name?.lowercase() } ?: emptyList()
                        for (tag in artistTags.take(5)) {
                            val exactMatch = userGenresLower.firstOrNull { it == tag }
                            val subMatch = if (exactMatch == null) userGenresLower.firstOrNull { it.contains(tag) || tag.contains(it) } else null
                            when {
                                exactMatch != null -> { score += 2; matchedGenre = userGenres[userGenresLower.indexOf(exactMatch)]; break }
                                subMatch != null  -> { score += 1; matchedGenre = userGenres[userGenresLower.indexOf(subMatch)] }
                            }
                            if (score >= 2) break
                        }
                    } catch (e: Exception) {
                        Log.w("ArtworkRepository", "Tag fetch failed for trending artist $artistName", e)
                    }
                }

                val imageUrl = track.image?.lastOrNull { it.text?.isNotBlank() == true &&
                    it.text.contains("2a96cbd8b46e442fc41c2b86b821562f") == false }?.text

                scored.add(ScoredTrack(track, score, matchedGenre, imageUrl))
                // Early exit: once we have enough high-scoring tracks, stop fetching tags
                if (scored.count { it.score > 0 } >= targetCount) break
            }

            // Sort: genre-matching tracks first, then by original chart position
            scored.sortByDescending { it.score }

            scored.take(targetCount).map { st ->
                TrendingItem(
                    trackTitle = st.track.name ?: "",
                    artistName = st.track.artist?.name ?: "",
                    imageUrl = st.imageUrl,
                    matchedGenre = st.matchedGenre
                )
            }
        } catch (e: Exception) {
            Log.e("ArtworkRepository", "Trending tracks fetch error", e)
            emptyList()
        }
    }
    private suspend fun fetchWikipediaSummary(artistName: String): WikipediaSummaryResponse? {
        try {
            // 1. Try guessing first (fast, no MusicBrainz rate limits)
            val title = java.net.URLEncoder.encode(artistName.replace(" ", "_"), "UTF-8")
            val response = wikipediaService.getSummary("https://en.wikipedia.org/api/rest_v1/page/summary/$title")
            val lowerExtract = response.extract?.lowercase() ?: ""
            // Ensure it's actually an artist and not a place/movie with the same name
            if (lowerExtract.contains("musician") || lowerExtract.contains("singer") || lowerExtract.contains("band") || lowerExtract.contains("rapper") || lowerExtract.contains("group") || lowerExtract.contains("artist")) {
                return response
            }
        } catch (e: Exception) {
             Log.w("ArtworkRepository", "Failed Wikipedia guess for $artistName")
        }

        try {
            // 2. Fallback: try direct MBID to wikipedia relation for perfect disambiguation
            val mbid = resolveArtistMbid(artistName)
            if (mbid != null) {
                val mbResponse = withMusicBrainzRateLimit { musicBrainzService.getArtistById(mbid) }
                val wikiUrl = mbResponse.relations?.firstOrNull { it.type == "wikipedia" || it.type == "wikidata" }?.url?.resource
                if (wikiUrl != null && wikiUrl.contains("wikipedia.org/wiki/")) {
                    val title = java.net.URLDecoder.decode(wikiUrl.substringAfterLast("/"), "UTF-8")
                    val response = wikipediaService.getSummary("https://en.wikipedia.org/api/rest_v1/page/summary/$title")
                    if (response.extract != null || response.originalimage?.source != null) return response
                }
            }
        } catch (e: Exception) {
            Log.w("ArtworkRepository", "Failed Wikipedia via MBID for $artistName")
        }
        return null
    }
}

// ---------------------------------------------------------------------------
// Data transfer objects used by Collection Growth (not persisted directly)
// ---------------------------------------------------------------------------

data class NewReleaseItem(
    val title: String,
    val artistName: String,
    val releaseType: String,
    val releaseDateStr: String,
    val imageUrl: String?
)

data class DiscoveryItem(
    val suggestedArtistName: String,
    val becauseOfArtist: String,
    val sharedGenre: String?,
    val imageUrl: String?
)

data class NewSongItem(
    val trackTitle: String,
    val artistName: String,
    val mbid: String,
    val releaseDateStr: String,
    val imageUrl: String?
)

data class TrendingItem(
    val trackTitle: String,
    val artistName: String,
    val imageUrl: String?,
    val matchedGenre: String?
)
