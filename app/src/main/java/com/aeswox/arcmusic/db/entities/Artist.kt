package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey val id: String,
    val name: String,
    val photoUri: String?,
    val bioText: String?,
    val isFavorite: Boolean = false,
    val missingTracksCount: Int? = null,
    val missingAlbumsCount: Int? = null,
    val hasScannedMissingContent: Boolean = false
)
