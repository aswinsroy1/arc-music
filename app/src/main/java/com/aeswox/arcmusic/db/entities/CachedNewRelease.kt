package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A recent release (album/single/EP) from a favorited or locally-owned artist that is not
 *  yet in the local library. Cached for 7 days; sourced from MusicBrainz release-groups. */
@Entity(tableName = "cached_new_releases")
data class CachedNewRelease(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artistName: String,
    /** "Album", "Single", or "EP" */
    val releaseType: String,
    /** ISO date string e.g. "2025-11-14" */
    val releaseDateStr: String,
    val imageUrl: String?,
    @androidx.room.ColumnInfo(defaultValue = "0")
    val cachedAt: Long = System.currentTimeMillis()
)
