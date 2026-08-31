package com.aeswox.arcmusic.playback

import android.content.Context
import com.aeswox.arcmusic.db.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun provideMusicPlayerConnection(
        @ApplicationContext context: Context,
        repository: MusicRepository,
        lyricsRepository: com.aeswox.arcmusic.data.repository.LyricsRepository
    ): MusicPlayerConnection {
        return MusicPlayerConnection(context, repository, lyricsRepository)
    }
}
