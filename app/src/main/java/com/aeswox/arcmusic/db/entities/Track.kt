package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val albumArtist: String?,
    val albumId: Long?,
    val album: String,
    val genre: String?,
    val composer: String?,
    val year: Int?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val durationMs: Long,
    val filePath: String,
    val fileSizeBytes: Long,
    val bitrate: Int?,
    val codec: String?,
    val artworkUri: String?,
    val sampleRate: Int?,
    val bitDepth: Int?,
    val dateAdded: Long,
    val dateModified: Long,
    val isFavorite: Boolean,
    val playCount: Int,
    val lastPlayedAt: Long?,
    val source: TrackSource,
    val remoteId: String?,
    val hasLyrics: Boolean = false,
    val lyricsSyncedAt: Long = 0L
)
