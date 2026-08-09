package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A recent individual track (not an album/EP) from a qualifying artist that is not yet in the
 * local library. Sourced from MusicBrainz recording search. Cached for 7 days.
 */
@Entity(tableName = "cached_new_songs")
data class CachedNewSong(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackTitle: String,
    val artistName: String,
    /** MusicBrainz recording MBID — used for deduplication. */
    val mbid: String,
    /** ISO date string, e.g. "2025-11-14" */
    val releaseDateStr: String,
    val imageUrl: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
