package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.PlayHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    @Query("SELECT * FROM play_history ORDER BY timestamp DESC")
    fun getFullPlayHistory(): Flow<List<PlayHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(playHistory: PlayHistory)

    @Query("UPDATE play_history SET completed = 1, playedMs = :playedMs WHERE id = (SELECT MAX(id) FROM play_history WHERE trackId = :trackId)")
    suspend fun markMostRecentCompleted(trackId: String, playedMs: Long)

    @Query("UPDATE play_history SET playedMs = :playedMs WHERE id = (SELECT MAX(id) FROM play_history WHERE trackId = :trackId)")
    suspend fun updatePlayedMs(trackId: String, playedMs: Long)
}
