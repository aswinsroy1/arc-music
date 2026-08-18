package com.aeswox.arcmusic.data.repository

import com.aeswox.arcmusic.data.model.Lyrics
import com.aeswox.arcmusic.db.entities.Track

interface LyricsRepository {
    suspend fun getLyrics(track: Track): Lyrics?
    suspend fun saveLyricsLocally(track: Track, lyricsText: String)
    suspend fun downloadAndSaveLyrics(track: Track): Boolean
    suspend fun hasLocalOrEmbeddedLyrics(track: Track): Boolean
}
