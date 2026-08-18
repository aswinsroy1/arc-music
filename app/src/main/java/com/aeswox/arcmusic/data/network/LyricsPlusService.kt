package com.aeswox.arcmusic.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the LyricsPlus API (https://lyricsplus.prjktla.my.id).
 * No API key required. Returns syllable/word-level or line-level synced lyrics
 * depending on source availability for the requested track.
 */
interface LyricsPlusService {

    /**
     * Fetch lyrics for a track.
     * @param title Track title.
     * @param artist Artist name.
     * @return [LyricsPlusResponse] on success, or null if not found.
     */
    @GET("v2/lyrics/get")
    suspend fun getLyrics(
        @Query("title") title: String,
        @Query("artist") artist: String
    ): LyricsPlusResponse?
}
