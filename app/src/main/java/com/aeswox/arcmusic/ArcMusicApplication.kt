package com.aeswox.arcmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.aeswox.arcmusic.data.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class ArcMusicApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun newImageLoader(): ImageLoader {
        val cacheLimitMb = runBlocking {
            settingsRepository.coilDiskCacheLimitMb.first()
        }
        
        return ImageLoader.Builder(this)
            .crossfade(true)
            .respectCacheHeaders(false)
            .placeholder(R.drawable.ic_music_placeholder)
            .error(R.drawable.ic_music_placeholder)
            .fallback(R.drawable.ic_music_placeholder)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(cacheLimitMb * 1024L * 1024L)
                    .build()
            }
            .build()
    }
}
