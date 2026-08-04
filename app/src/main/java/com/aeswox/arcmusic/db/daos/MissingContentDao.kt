package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.CachedMissingContent

@Dao
interface MissingContentDao {
    @Query("SELECT * FROM cached_missing_content WHERE artistName = :artistName")
    suspend fun getByArtistName(artistName: String): List<CachedMissingContent>

    @Query("SELECT MIN(cachedAt) FROM cached_missing_content WHERE artistName = :artistName")
    suspend fun getOldestCachedAt(artistName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedMissingContent>)

    @Query("DELETE FROM cached_missing_content WHERE artistName = :artistName")
    suspend fun deleteAllByArtist(artistName: String)

    @Query("DELETE FROM cached_missing_content WHERE cachedAt < :cutoffMs")
    suspend fun deleteStale(cutoffMs: Long)
}
