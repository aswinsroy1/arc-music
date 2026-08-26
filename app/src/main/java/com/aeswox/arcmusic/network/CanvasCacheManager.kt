package com.aeswox.arcmusic.network

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.aeswox.arcmusic.data.SettingsRepository

@UnstableApi
@Singleton
class CanvasCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null
    private var currentLimitBytes: Long = 0L

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val initialLimitMb = settingsRepository.canvasCacheLimitMb.first()
            currentLimitBytes = initialLimitMb * 1024L * 1024L
            initializeCache()
        }
    }

    @Synchronized
    private fun initializeCache() {
        if (simpleCache != null) return
        val cacheDir = File(context.cacheDir, "canvas_cache")
        val evictor = LeastRecentlyUsedCacheEvictor(currentLimitBytes)
        val databaseProvider = StandaloneDatabaseProvider(context)
        
        simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        
        val upstreamFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache!!)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @Synchronized
    fun getCacheDataSourceFactory(): CacheDataSource.Factory {
        if (cacheDataSourceFactory == null) {
            initializeCache()
        }
        return cacheDataSourceFactory!!
    }

    @Synchronized
    fun getCache(): SimpleCache {
        if (simpleCache == null) {
            initializeCache()
        }
        return simpleCache!!
    }

    @Synchronized
    fun clearCache() {
        simpleCache?.let {
            for (key in it.keys) {
                it.removeResource(key)
            }
        }
    }

    fun getCacheSizeBytes(): Long {
        return simpleCache?.cacheSpace ?: 0L
    }
}
