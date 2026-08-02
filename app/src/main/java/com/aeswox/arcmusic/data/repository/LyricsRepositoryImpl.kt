package com.aeswox.arcmusic.data.repository

import android.util.Log
import com.aeswox.arcmusic.data.model.Lyrics
import com.aeswox.arcmusic.data.model.SyncedLine
import com.aeswox.arcmusic.data.model.SyncedWord
import com.aeswox.arcmusic.data.network.LrcLibApiService
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.utils.LyricsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val lrcLibApiService: LrcLibApiService
) : LyricsRepository {

    private val lyricsCache = mutableMapOf<String, Lyrics>()

    override suspend fun getLyrics(track: Track): Lyrics? = withContext(Dispatchers.IO) {
        val cacheKey = track.id
        
        lyricsCache[cacheKey]?.let {
            return@withContext it
        }

        val fetchers = listOf(
            suspend { loadEmbeddedLyrics(track) },
            suspend { loadLocalFileLyrics(track) },
            suspend { fetchFromLrcLib(track) }
        )

        for ((index, fetcher) in fetchers.withIndex()) {
            try {
                val lyrics = fetcher()
                if (lyrics != null && (lyrics.plain != null || lyrics.synced != null)) {
                    Log.d("Lyrics", "Found lyrics from source ${index + 1} for ${track.title}")
                    lyricsCache[cacheKey] = lyrics
                    return@withContext lyrics
                }
            } catch (e: Exception) {
                Log.w("Lyrics", "Error fetching from source ${index + 1}: ${e.message}")
            }
        }
        null
    }

    private fun loadEmbeddedLyrics(track: Track): Lyrics? {
        try {
            val file = File(track.filePath)
            if (!file.exists()) return null

            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag ?: return null

            // Try SYLT (Synchronized) or USLT (Unsynchronized)
            val lyricsString = tag.getFirst(FieldKey.LYRICS)
            if (!lyricsString.isNullOrBlank()) {
                val parsed = LyricsUtils.parseLyrics(lyricsString)
                if (parsed != null) return parsed
            }
        } catch (e: Exception) {
            Log.w("Lyrics", "Failed to read embedded lyrics: ${e.message}")
        }
        return null
    }

    private fun loadLocalFileLyrics(track: Track): Lyrics? {
        val audioFile = File(track.filePath)
        if (!audioFile.exists()) return null

        val dir = audioFile.parentFile ?: return null
        val baseName = audioFile.nameWithoutExtension

        val possibleExts = listOf(".lrc", ".txt")
        for (ext in possibleExts) {
            val lyricFile = File(dir, "$baseName$ext")
            if (lyricFile.exists()) {
                val content = lyricFile.readText()
                val parsed = LyricsUtils.parseLyrics(content)
                if (parsed != null) return parsed
            }
        }
        return null
    }

    private suspend fun fetchFromLrcLib(track: Track): Lyrics? {
        val durationSec = (track.durationMs / 1000).toInt()
        val res = lrcLibApiService.getLyrics(
            trackName = track.title,
            artistName = track.artist,
            albumName = track.album,
            duration = durationSec
        )
        
        if (res != null) {
            val synced = res.syncedLyrics
            if (!synced.isNullOrBlank()) {
                return LyricsUtils.parseLyrics(synced)
            }
            val plain = res.plainLyrics
            if (!plain.isNullOrBlank()) {
                return LyricsUtils.parseLyrics(plain)
            }
        }
        return null
    }
}
