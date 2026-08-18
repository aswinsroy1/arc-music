package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists")
    fun getAllArtists(): Flow<List<Artist>>

    @Query("SELECT * FROM artists WHERE id = :id")
    fun getArtistById(id: String): Flow<Artist?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<Artist>)

    @Query("DELETE FROM artists WHERE name IN (:artistNames)")
    suspend fun deleteArtists(artistNames: List<String>)

    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()

    @Query("UPDATE artists SET isFavorite = :isFavorite WHERE id = :artistId")
    suspend fun updateArtistFavoriteStatus(artistId: String, isFavorite: Boolean)

    @Query("UPDATE artists SET hasScannedMissingContent = :hasScanned WHERE id = :artistId")
    suspend fun updateHasScannedMissingContent(artistId: String, hasScanned: Boolean)

    @Query("UPDATE artists SET photoUri = :photoUri WHERE id = :artistId")
    suspend fun updateArtistPhoto(artistId: String, photoUri: String?)

    @Query("UPDATE artists SET bioText = :bioText WHERE id = :artistId")
    suspend fun updateArtistBio(artistId: String, bioText: String?)

    @Query("UPDATE artists SET missingTracksCount = :missingTracks, missingAlbumsCount = :missingAlbums WHERE id = :artistId")
    suspend fun updateArtistGaps(artistId: String, missingTracks: Int, missingAlbums: Int)

    @Query("SELECT * FROM artists WHERE name LIKE :query || '%' OR name LIKE '% ' || :query || '%' LIMIT 10")
    fun searchArtists(query: String): Flow<List<Artist>>
}
