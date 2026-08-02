package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val dateCreated: Long,
    val coverArtUri: String? = null,
    val description: String? = null
)
