package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val year: Int?,
    val artworkUri: String?,
    val trackCount: Int,
    val isFavorite: Boolean = false
)
