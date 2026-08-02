import re
import os

# Create directory
os.makedirs('app/src/main/java/com/example/db', exist_ok=True)

# 1. Entity.kt
entity_code = """package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val category: String, // e.g., "RecentlyPlayed", "RandomPicks", "NewReleases", "LibraryAlbums", "LibraryArtists", "LibraryTracks", "GenreTopTracks"
    val extraData: String = "" // For things like "1.2M plays"
)
"""
with open('app/src/main/java/com/example/db/Entity.kt', 'w') as f:
    f.write(entity_code)

# 2. MusicDao.kt
dao_code = """package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM songs WHERE category = :category")
    fun getSongsByCategory(category: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getCount(): Int
}
"""
with open('app/src/main/java/com/example/db/MusicDao.kt', 'w') as f:
    f.write(dao_code)

# 3. MusicDatabase.kt
db_code = """package com.example.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SongEntity::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
"""
with open('app/src/main/java/com/example/db/MusicDatabase.kt', 'w') as f:
    f.write(db_code)

# 4. MusicRepository.kt
repo_code = """package com.example.db

import kotlinx.coroutines.flow.Flow

class MusicRepository(private val musicDao: MusicDao) {
    fun getSongsByCategory(category: String): Flow<List<SongEntity>> {
        return musicDao.getSongsByCategory(category)
    }

    suspend fun seedDatabaseIfNeeded(seedData: List<SongEntity>) {
        if (musicDao.getCount() == 0) {
            musicDao.insertSongs(seedData)
        }
    }
}
"""
with open('app/src/main/java/com/example/db/MusicRepository.kt', 'w') as f:
    f.write(repo_code)

print("Database files generated.")
