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
    val lyricsSyncedAt: Long = 0L,
    val canvasUrl: String? = null,
    val canvasSyncedAt: Long = 0L,
    val isExplicit: Boolean? = null
)


fun Track.getQualityBadgeResId(): Int? {
    val codecLower = this.codec?.lowercase() ?: ""
    val pathLower = this.filePath?.lowercase() ?: ""
    val isAtmos = codecLower.contains("atmos") || codecLower.contains("eac3") || codecLower.contains("ac3") || codecLower.contains("ec-3") || pathLower.endsWith(".eac3") || pathLower.endsWith(".ac3")
    if (isAtmos) return com.aeswox.arcmusic.R.drawable.ic_dolby
    
    val isLossless = this.codec?.let { c -> 
        listOf("flac", "alac", "wav", "ape", "dsd").any { c.contains(it, ignoreCase = true) } 
    } == true
    
    val sample = this.sampleRate ?: 0
    val bit = this.bitDepth ?: 0
    val bitr = this.bitrate ?: 0
    
    if (isLossless) {
        if (sample > 48000 || bit > 16) return com.aeswox.arcmusic.R.drawable.ic_high_res
        if (sample >= 44100 && bit >= 16) return com.aeswox.arcmusic.R.drawable.ic_cd
        return com.aeswox.arcmusic.R.drawable.ic_cd
    } else {
        if (bitr >= 320000) return com.aeswox.arcmusic.R.drawable.ic_hq
    }
    return null
}
