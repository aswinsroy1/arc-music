package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums")
    fun getAllAlbums(): Flow<List<Album>>

    @Query("SELECT * FROM albums WHERE id = :id")
    fun getAlbumById(id: String): Flow<Album?>

    @Query("SELECT * FROM albums WHERE artist = :artistName")
    fun getAlbumsByArtist(artistName: String): Flow<List<Album>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<Album>)

    @Query("DELETE FROM albums WHERE title IN (:albumTitles)")
    suspend fun deleteAlbums(albumTitles: List<String>)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()

    @Query("UPDATE albums SET isFavorite = :isFavorite WHERE id = :albumId")
    suspend fun updateAlbumFavoriteStatus(albumId: String, isFavorite: Boolean)

    @Query("UPDATE albums SET artworkUri = :artworkUri WHERE id = :albumId")
    suspend fun updateAlbumArtwork(albumId: String, artworkUri: String?)

    @Query("SELECT * FROM albums WHERE title LIKE :query || '%' OR title LIKE '% ' || :query || '%' OR artist LIKE :query || '%' OR artist LIKE '% ' || :query || '%' LIMIT 10")
    fun searchAlbums(query: String): Flow<List<Album>>
}
