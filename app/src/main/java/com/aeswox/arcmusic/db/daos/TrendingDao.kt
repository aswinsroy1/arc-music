package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.CachedTrending

@Dao
interface TrendingDao {
    @Query("SELECT * FROM cached_trending ORDER BY id ASC")
    suspend fun getAll(): List<CachedTrending>

    @Query("SELECT MIN(cachedAt) FROM cached_trending")
    suspend fun getOldestCachedAt(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedTrending>)

    @Query("DELETE FROM cached_trending")
    suspend fun clearAll()
}
