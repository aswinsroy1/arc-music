package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_missing_content")
data class CachedMissingContent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artistName: String,
    val isAlbum: Boolean,
    val imageUrl: String?,
    val missingCount: Int = 0
)
