package com.aeswox.arcmusic.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_history",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("trackId")
    ]
)
data class PlayHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val timestamp: Long,
    val playedMs: Long = 0L,   // actual milliseconds the user listened (0 = unknown / legacy row)
    val completed: Boolean,
    val skipReason: String?
)
