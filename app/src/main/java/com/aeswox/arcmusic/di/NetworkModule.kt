package com.aeswox.arcmusic.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.aeswox.arcmusic.data.network.DeezerService
import com.aeswox.arcmusic.data.network.LastFmService
import com.aeswox.arcmusic.data.network.TheAudioDbService
import com.aeswox.arcmusic.data.network.MusicBrainzService
import okhttp3.OkHttpClient
import javax.inject.Singleton
import dagger.Provides

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideDeezerRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideDeezerService(retrofit: Retrofit): DeezerService {
        return retrofit.create(DeezerService::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "ArcMusic/1.0 ( placeholder@email.com )")
                .build()
            chain.proceed(request)
        }.build()
    }

    @Provides
    @Singleton
    fun provideLastFmService(moshi: Moshi, okHttpClient: OkHttpClient): LastFmService {
        return Retrofit.Builder()
            .baseUrl("http://ws.audioscrobbler.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LastFmService::class.java)
    }

    @Provides
    @Singleton
    fun provideTheAudioDbService(moshi: Moshi, okHttpClient: OkHttpClient): TheAudioDbService {
        return Retrofit.Builder()
            .baseUrl("https://theaudiodb.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TheAudioDbService::class.java)
    }

    @Provides
    @Singleton
    fun provideMusicBrainzService(moshi: Moshi, okHttpClient: OkHttpClient): MusicBrainzService {
        return Retrofit.Builder()
            .baseUrl("https://musicbrainz.org/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MusicBrainzService::class.java)
    }

    @Provides
    @Singleton
    fun provideLrcLibService(moshi: Moshi, okHttpClient: OkHttpClient): com.aeswox.arcmusic.data.network.LrcLibApiService {
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(com.aeswox.arcmusic.data.network.LrcLibApiService::class.java)
    }
}
