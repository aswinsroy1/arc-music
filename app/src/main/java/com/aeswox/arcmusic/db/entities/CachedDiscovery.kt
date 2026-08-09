package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_discoveries")
data class CachedDiscovery(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val suggestedArtistName: String,
    val becauseOfArtist: String,
    val sharedGenre: String?,
    val imageUrl: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
