package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.CachedDiscovery
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscoveryDao {
    @Query("SELECT * FROM cached_discoveries")
    fun getAll(): Flow<List<CachedDiscovery>>

    @Query("SELECT * FROM cached_discoveries WHERE becauseOfArtist = :artistName")
    suspend fun getByBecauseOfArtist(artistName: String): List<CachedDiscovery>

    @Query("SELECT MIN(cachedAt) FROM cached_discoveries WHERE becauseOfArtist = :artistName")
    suspend fun getOldestCachedAt(artistName: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(discoveries: List<CachedDiscovery>)

    @Query("DELETE FROM cached_discoveries WHERE becauseOfArtist = :artistName")
    suspend fun deleteAllByBecauseOfArtist(artistName: String)

    @Query("DELETE FROM cached_discoveries")
    suspend fun clearAll()
}
