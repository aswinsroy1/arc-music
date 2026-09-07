package com.aeswox.arcmusic.network

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanvasCoordinator @Inject constructor(
    private val appleMusicCanvasProvider: AppleMusicCanvasProvider,
    private val spotifyCanvasProvider: SpotifyCanvasProvider
) {

    /**
     * Attempts to fetch the Canvas URL based on the given priority ("apple" or "spotify").
     * Falls back to the alternative provider if the priority provider fails to return a Canvas.
     */
    suspend fun getCanvasUrl(
        title: String,
        artist: String,
        album: String?,
        priority: String,
        spotifySpDcCookie: String?
    ): Pair<String, String>? {
        return if (priority == "spotify") {
            // Priority: Spotify -> Apple Music
            val spotifyUrl = spotifySpDcCookie?.takeIf { it.isNotBlank() }?.let {
                Log.d("CanvasCoordinator", "Attempting Spotify Canvas first...")
                spotifyCanvasProvider.getCanvasUrl(title, artist, it)
            }
            if (spotifyUrl != null) {
                Pair(spotifyUrl, "spotify")
            } else {
                Log.d("CanvasCoordinator", "Spotify failed or not configured, falling back to Apple Music...")
                appleMusicCanvasProvider.getCanvasUrl(title, artist, album)?.let { Pair(it, "apple") }
            }
        } else {
            // Priority: Apple Music -> Spotify
            Log.d("CanvasCoordinator", "Attempting Apple Music Canvas first...")
            val appleUrl = appleMusicCanvasProvider.getCanvasUrl(title, artist, album)
            if (appleUrl != null) {
                Pair(appleUrl, "apple")
            } else {
                val spotifyUrl = spotifySpDcCookie?.takeIf { it.isNotBlank() }?.let {
                    Log.d("CanvasCoordinator", "Apple Music failed, falling back to Spotify...")
                    spotifyCanvasProvider.getCanvasUrl(title, artist, it)
                }
                spotifyUrl?.let { Pair(it, "spotify") }
            }
        }
    }
}
