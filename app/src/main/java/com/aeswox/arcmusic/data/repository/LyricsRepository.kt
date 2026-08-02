package com.aeswox.arcmusic.data.repository

import com.aeswox.arcmusic.data.model.Lyrics
import com.aeswox.arcmusic.db.entities.Track

interface LyricsRepository {
    suspend fun getLyrics(track: Track): Lyrics?
}
