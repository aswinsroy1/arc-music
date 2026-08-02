package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "tracks_fts")
@Fts4(contentEntity = Track::class)
data class TrackFts(
    val title: String,
    val artist: String
)
