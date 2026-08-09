package com.aeswox.arcmusic.db.entities

import androidx.room.Entity

/** Persists a user's "dismiss" action on a Collection Growth feed card so it never
 *  reappears. The composite primary key uniquely identifies each dismissable card. */
@Entity(
    tableName = "dismissed_growth_cards",
    primaryKeys = ["cardType", "title", "artistName"]
)
data class DismissedGrowthCard(
    /** e.g. "complete_collection", "new_release", "discovery", "missing_tracks" */
    val cardType: String,
    val title: String,
    val artistName: String,
    val dismissedAt: Long = System.currentTimeMillis()
)
