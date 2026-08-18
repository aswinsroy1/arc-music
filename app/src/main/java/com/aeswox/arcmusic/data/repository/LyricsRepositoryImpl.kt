package com.aeswox.arcmusic.data.repository

import android.util.Log
import com.aeswox.arcmusic.data.model.Lyrics
import com.aeswox.arcmusic.data.model.SyncedLine
import com.aeswox.arcmusic.data.model.SyncedWord
import com.aeswox.arcmusic.data.network.LrcLibApiService
import com.aeswox.arcmusic.data.network.LyricsPlusService
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.utils.LyricsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lrcLibApiService: LrcLibApiService,
    private val lyricsPlusService: LyricsPlusService
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
            suspend { fetchFromLyricsPlus(track) },
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
        // Check next to audio file first
        val audioFile = File(track.filePath)
        if (audioFile.exists()) {
            val dir = audioFile.parentFile
            if (dir != null) {
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
            }
        }
        
        // Then check internal storage cache
        val internalDir = File(context.filesDir, "lyrics")
        val internalLyricFile = File(internalDir, "${track.id}.lrc")
        if (internalLyricFile.exists()) {
            val content = internalLyricFile.readText()
            val parsed = LyricsUtils.parseLyrics(content)
            if (parsed != null) return parsed
        }
        
        return null
    }

    override suspend fun saveLyricsLocally(track: Track, lyricsText: String): Unit = withContext(Dispatchers.IO) {
        try {
            val internalDir = File(context.filesDir, "lyrics")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }
            val internalLyricFile = File(internalDir, "${track.id}.lrc")
            internalLyricFile.writeText(lyricsText)
        } catch (e: Exception) {
            Log.e("Lyrics", "Failed to save lyrics locally", e)
        }
    }

    override suspend fun downloadAndSaveLyrics(track: Track): Boolean = withContext(Dispatchers.IO) {
        if (hasLocalOrEmbeddedLyrics(track)) {
            return@withContext true
        }
        
        try {
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
                    saveLyricsLocally(track, synced)
                    return@withContext true
                }
                val plain = res.plainLyrics
                if (!plain.isNullOrBlank()) {
                    saveLyricsLocally(track, plain)
                    return@withContext true
                }
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e("Lyrics", "Failed to download lyrics for ${track.title}", e)
            return@withContext false
        }
    }

    override suspend fun hasLocalOrEmbeddedLyrics(track: Track): Boolean = withContext(Dispatchers.IO) {
        if (loadLocalFileLyrics(track) != null) return@withContext true
        
        try {
            val file = File(track.filePath)
            if (file.exists()) {
                val audioFile = AudioFileIO.read(file)
                val tag = audioFile.tag
                if (tag != null) {
                    val lyricsString = tag.getFirst(FieldKey.LYRICS)
                    if (!lyricsString.isNullOrBlank()) return@withContext true
                }
            }
        } catch (e: Exception) {
            // Ignore extraction issues
        }
        
        return@withContext false
    }

    private suspend fun fetchFromLyricsPlus(track: Track): Lyrics? {
        val res = lyricsPlusService.getLyrics(
            title = track.title,
            artist = track.artist
        )
        val lines = res?.lyrics
        if (lines.isNullOrEmpty()) return null

        val isWordLevel = res.type?.equals("Word", ignoreCase = true) == true

        val syncedLines = lines.mapNotNull { line ->
            val lineText = line.text?.trim() ?: return@mapNotNull null
            val lineTime = line.time.toInt()

            val words: List<SyncedWord>? = if (isWordLevel && !line.syllabus.isNullOrEmpty()) {
                line.syllabus.mapNotNull { syl ->
                    val rawText = syl.text ?: return@mapNotNull null
                    val syllableText = rawText.trim()
                    if (syllableText.isEmpty()) return@mapNotNull null
                    // A trailing space in the raw syllable text signals a word boundary
                    val startsNewWord = line.syllabus.indexOf(syl) == 0 ||
                        (line.syllabus.getOrNull(line.syllabus.indexOf(syl) - 1)?.text?.endsWith(" ") == true)
                    SyncedWord(
                        time = syl.time.toInt(),
                        word = syllableText,
                        startsNewWord = startsNewWord
                    )
                }.takeIf { it.isNotEmpty() }
            } else {
                // LINE-level: leave words null so the existing line-display path handles it
                null
            }

            SyncedLine(
                time = lineTime,
                line = lineText,
                words = words
            )
        }

        return if (syncedLines.isNotEmpty()) {
            Lyrics(plain = null, synced = syncedLines, areFromRemote = true)
        } else {
            null
        }
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
