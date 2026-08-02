package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.aeswox.arcmusic.db.entities.Track

private val TRACK_SEARCH_QUERY_TOKEN_REGEX = Regex("""[\p{L}\p{N}]+""")
private const val EMPTY_TRACK_SEARCH_MATCH_QUERY = "arcmusicemptyquery*"

private fun buildTrackSearchMatchQuery(query: String): String {
    val tokens = TRACK_SEARCH_QUERY_TOKEN_REGEX
        .findAll(query)
        .map { it.value.trim() }
        .filter { it.isNotEmpty() }
        .take(6)
        .toList()

    if (tokens.isEmpty()) return EMPTY_TRACK_SEARCH_MATCH_QUERY

    return tokens.joinToString(separator = " AND ") { "${it}*" }
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks WHERE filePath LIKE '%eac3%' OR filePath LIKE '%ac3%' OR codec LIKE '%eac3%' OR codec LIKE '%ac3%' LIMIT 1")
    suspend fun getEac3Track(): Track?

    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE album = :albumTitle")
    fun getTracksByAlbum(albumTitle: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE artist = :artistName OR albumArtist = :artistName")
    fun getTracksByArtist(artistName: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentlyPlayedTracks(limit: Int): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY RANDOM() LIMIT :limit")
    fun getRandomTracks(limit: Int): Flow<List<Track>>

    // For Collection Health
    @Query("SELECT * FROM tracks WHERE genre IS NULL OR genre = ''")
    fun getTracksMissingGenre(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE artworkUri IS NULL OR artworkUri = ''")
    fun getTracksMissingArtwork(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE title IS NULL OR title = '' OR artist IS NULL OR artist = '' OR album IS NULL OR album = '' OR genre IS NULL OR genre = ''")
    fun getTracksMissingMetadata(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE bitrate < 192000")
    fun getLowQualityTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE durationMs = 0 OR durationMs IS NULL")
    fun getCorruptedTracks(): Flow<List<Track>>

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE id = :trackId")
    suspend fun incrementPlayCountAndUpdateLastPlayed(trackId: String, timestamp: Long)

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :trackId")
    suspend fun updateTrackFavoriteStatus(trackId: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET artworkUri = :artworkUri WHERE id = :trackId")
    suspend fun updateTrackArtwork(trackId: String, artworkUri: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Query("DELETE FROM tracks WHERE id IN (:trackIds)")
    suspend fun deleteTracks(trackIds: List<String>)

    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN tracks_fts ON tracks_fts.docid = tracks.id
        WHERE tracks_fts MATCH :matchQuery
        LIMIT 50
    """)
    fun searchTracksMatch(matchQuery: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE title LIKE :query || '%' OR title LIKE '% ' || :query || '%' OR artist LIKE :query || '%' OR artist LIKE '% ' || :query || '%' LIMIT 50")
    fun searchTracksLike(query: String): Flow<List<Track>>

    fun searchTracks(query: String): Flow<List<Track>> {
        val ftsFlow = searchTracksMatch(buildTrackSearchMatchQuery(query))
        val likeFlow = searchTracksLike(query.trim())
        return ftsFlow.combine(likeFlow) { ftsResults, likeResults ->
            val seen = LinkedHashMap<String, Track>(ftsResults.size + likeResults.size)
            ftsResults.forEach { seen.putIfAbsent(it.id, it) }
            likeResults.forEach { seen.putIfAbsent(it.id, it) }
            seen.values.toList().take(30)
        }
    }

    @Query("SELECT DISTINCT genre FROM tracks WHERE genre IS NOT NULL AND genre != ''")
    fun getAllGenres(): Flow<List<String>>
}
