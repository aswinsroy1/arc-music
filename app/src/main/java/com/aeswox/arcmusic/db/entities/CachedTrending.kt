package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A globally-trending track (Last.fm chart.getTopTracks) that is not in the local library
 * and has been ranked/biased toward the user's top genres. Gated on Last.fm API key.
 * Cached for 7 days.
 */
@Entity(tableName = "cached_trending")
data class CachedTrending(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackTitle: String,
    val artistName: String,
    val imageUrl: String?,
    /** The user genre tag that caused this track to score, if any. */
    val matchedGenre: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
