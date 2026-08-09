package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.CachedNewSong

@Dao
interface NewSongDao {
    @Query("SELECT * FROM cached_new_songs WHERE artistName = :artistName")
    suspend fun getByArtistName(artistName: String): List<CachedNewSong>

    @Query("SELECT * FROM cached_new_songs")
    suspend fun getAll(): List<CachedNewSong>

    @Query("SELECT MIN(cachedAt) FROM cached_new_songs WHERE artistName = :artistName")
    suspend fun getOldestCachedAt(artistName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedNewSong>)

    @Query("DELETE FROM cached_new_songs WHERE artistName = :artistName")
    suspend fun deleteAllByArtist(artistName: String)

    @Query("DELETE FROM cached_new_songs WHERE cachedAt < :cutoffMs")
    suspend fun deleteStale(cutoffMs: Long)
}
