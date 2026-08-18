package com.aeswox.arcmusic.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.aeswox.arcmusic.db.entities.*
import com.aeswox.arcmusic.db.daos.*

@Database(
    entities = [
        Track::class, 
        Album::class, 
        Artist::class, 
        Playlist::class, 
        PlaylistTrack::class, 
        PlayHistory::class,
        SearchHistory::class,
        TrackFts::class,
        CachedMissingContent::class,
        CachedNewRelease::class,
        DismissedGrowthCard::class,
        CachedDiscovery::class,
        CachedNewSong::class,
        CachedTrending::class
    ], 
    version = 20, 
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun missingContentDao(): MissingContentDao
    abstract fun newReleaseDao(): NewReleaseDao
    abstract fun dismissedCardDao(): DismissedCardDao
    abstract fun discoveryDao(): DiscoveryDao
    abstract fun newSongDao(): NewSongDao
    abstract fun trendingDao(): TrendingDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlists ADD COLUMN coverArtUri TEXT")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlists ADD COLUMN description TEXT")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE albums ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE artists ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracks ADD COLUMN artworkUri TEXT")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `search_history` (`query` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`query`))")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `tracks_fts` USING FTS4(`title`, `artist`, content=`tracks`)")
                db.execSQL("INSERT INTO tracks_fts(tracks_fts) VALUES ('rebuild')")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE artists ADD COLUMN missingTracksCount INTEGER")
                db.execSQL("ALTER TABLE artists ADD COLUMN missingAlbumsCount INTEGER")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE artists ADD COLUMN hasScannedMissingContent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cached_missing_content` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artistName` TEXT NOT NULL,
                        `isAlbum` INTEGER NOT NULL,
                        `imageUrl` TEXT,
                        `missingCount` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_missing_content ADD COLUMN missingTrackNamesJson TEXT")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_missing_content ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_missing_content ADD COLUMN isSingle INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cached_new_releases` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artistName` TEXT NOT NULL,
                        `releaseType` TEXT NOT NULL,
                        `releaseDateStr` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `cachedAt` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `dismissed_growth_cards` (
                        `cardType` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `artistName` TEXT NOT NULL,
                        `dismissedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`cardType`, `title`, `artistName`)
                    )
                """.trimIndent())
            }
        }
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Empty migration to bump schema hash. The tables are already structurally correct.
            }
        }
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cached_discoveries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `suggestedArtistName` TEXT NOT NULL,
                        `becauseOfArtist` TEXT NOT NULL,
                        `sharedGenre` TEXT,
                        `imageUrl` TEXT,
                        `cachedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cached_new_songs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trackTitle` TEXT NOT NULL,
                        `artistName` TEXT NOT NULL,
                        `mbid` TEXT NOT NULL,
                        `releaseDateStr` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `cachedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cached_trending` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trackTitle` TEXT NOT NULL,
                        `artistName` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `matchedGenre` TEXT,
                        `cachedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracks ADD COLUMN hasLyrics INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tracks ADD COLUMN lyricsSyncedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "music_database"
                )
                .addMigrations(
                    MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8,
                    MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12,
                    MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16,
                    MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
