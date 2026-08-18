package com.aeswox.arcmusic.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.aeswox.arcmusic.db.MusicDatabase
import com.aeswox.arcmusic.db.MusicRepository
import com.aeswox.arcmusic.db.daos.*
import com.aeswox.arcmusic.data.MediaStoreScanner

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase {
        return MusicDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: MusicDatabase) = database.trackDao()

    @Provides
    @Singleton
    fun provideAlbumDao(database: MusicDatabase) = database.albumDao()

    @Provides
    @Singleton
    fun provideArtistDao(database: MusicDatabase) = database.artistDao()

    @Provides
    @Singleton
    fun providePlaylistDao(database: MusicDatabase) = database.playlistDao()

    @Provides
    @Singleton
    fun providePlayHistoryDao(database: MusicDatabase) = database.playHistoryDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(database: MusicDatabase): SearchHistoryDao {
        return database.searchHistoryDao()
    }

    @Provides
    @Singleton
    fun provideMissingContentDao(database: MusicDatabase): MissingContentDao {
        return database.missingContentDao()
    }

    @Provides
    @Singleton
    fun provideNewReleaseDao(database: MusicDatabase): NewReleaseDao {
        return database.newReleaseDao()
    }

    @Provides
    @Singleton
    fun provideDismissedCardDao(database: MusicDatabase): DismissedCardDao {
        return database.dismissedCardDao()
    }

    @Provides
    @Singleton
    fun provideDiscoveryDao(database: MusicDatabase): DiscoveryDao {
        return database.discoveryDao()
    }

    @Provides
    @Singleton
    fun provideNewSongDao(database: MusicDatabase): NewSongDao {
        return database.newSongDao()
    }

    @Provides
    @Singleton
    fun provideTrendingDao(database: MusicDatabase): TrendingDao {
        return database.trendingDao()
    }

    @Provides
    @Singleton
    fun provideMusicRepository(
        @ApplicationContext context: android.content.Context,
        trackDao: TrackDao,
        albumDao: AlbumDao,
        artistDao: ArtistDao,
        playlistDao: PlaylistDao,
        playHistoryDao: PlayHistoryDao,
        searchHistoryDao: SearchHistoryDao,
        missingContentDao: MissingContentDao,
        newReleaseDao: NewReleaseDao,
        dismissedCardDao: DismissedCardDao,
        discoveryDao: DiscoveryDao,
        newSongDao: NewSongDao,
        trendingDao: TrendingDao,
        mediaStoreScanner: MediaStoreScanner,
        artworkRepository: com.aeswox.arcmusic.data.network.ArtworkRepository,
        lyricsRepository: com.aeswox.arcmusic.data.repository.LyricsRepository,
        deezerService: com.aeswox.arcmusic.data.network.DeezerService
    ): MusicRepository {
        return MusicRepository(
            context, trackDao, albumDao, artistDao, playlistDao, playHistoryDao,
            searchHistoryDao, missingContentDao, newReleaseDao, dismissedCardDao,
            discoveryDao, newSongDao, trendingDao, mediaStoreScanner, artworkRepository, lyricsRepository, deezerService
        )
    }

    @Provides
    @Singleton
    fun provideLyricsRepository(
        impl: com.aeswox.arcmusic.data.repository.LyricsRepositoryImpl
    ): com.aeswox.arcmusic.data.repository.LyricsRepository {
        return impl
    }
}
