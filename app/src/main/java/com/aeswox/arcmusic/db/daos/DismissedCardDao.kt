package com.aeswox.arcmusic.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeswox.arcmusic.db.entities.DismissedGrowthCard

@Dao
interface DismissedCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dismiss(card: DismissedGrowthCard)

    @Query("SELECT COUNT(*) > 0 FROM dismissed_growth_cards WHERE cardType = :cardType AND title = :title AND artistName = :artistName")
    suspend fun isDismissed(cardType: String, title: String, artistName: String): Boolean

    @Query("SELECT * FROM dismissed_growth_cards")
    suspend fun getAll(): List<DismissedGrowthCard>
}
