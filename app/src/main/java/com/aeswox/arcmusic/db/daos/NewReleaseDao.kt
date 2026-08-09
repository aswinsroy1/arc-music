package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.CachedNewRelease

@Dao
interface NewReleaseDao {
    @Query("SELECT * FROM cached_new_releases WHERE artistName = :artistName")
    suspend fun getByArtistName(artistName: String): List<CachedNewRelease>

    @Query("SELECT * FROM cached_new_releases")
    suspend fun getAll(): List<CachedNewRelease>

    @Query("SELECT MIN(cachedAt) FROM cached_new_releases WHERE artistName = :artistName")
    suspend fun getOldestCachedAt(artistName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedNewRelease>)

    @Query("DELETE FROM cached_new_releases WHERE artistName = :artistName")
    suspend fun deleteAllByArtist(artistName: String)

    @Query("DELETE FROM cached_new_releases WHERE cachedAt < :cutoffMs")
    suspend fun deleteStale(cutoffMs: Long)
}
