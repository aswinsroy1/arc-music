package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.Playlist
import com.aeswox.arcmusic.db.entities.PlaylistTrack
import com.aeswox.arcmusic.db.entities.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE name = :playlistName LIMIT 1")
    fun getPlaylist(playlistName: String): Flow<Playlist?>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        INNER JOIN playlists p ON pt.playlistId = p.id
        WHERE p.name = :playlistName
        ORDER BY pt.position ASC
    """)
    fun getTracksForPlaylist(playlistName: String): Flow<List<Track>>

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON t.id = pt.trackId
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.position ASC
    """)
    fun getTracksForPlaylistById(playlistId: String): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(playlistTracks: List<PlaylistTrack>)

    @Query("DELETE FROM playlists WHERE name IN (:playlistNames)")
    suspend fun deletePlaylists(playlistNames: List<String>)

    @Query("SELECT * FROM playlists WHERE name LIKE :query || '%' OR name LIKE '% ' || :query || '%' LIMIT 10")
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    @Query("SELECT DISTINCT playlistId FROM playlist_tracks WHERE trackId IN (:trackIds)")
    fun getPlaylistsContainingTracks(trackIds: List<String>): Flow<List<String>>
}
