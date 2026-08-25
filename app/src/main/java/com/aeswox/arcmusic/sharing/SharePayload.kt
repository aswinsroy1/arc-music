package com.aeswox.arcmusic.sharing

sealed class SharePayload {
    data class SingleTrack(val trackId: String) : SharePayload()
    data class MultipleTracks(val trackIds: List<String>) : SharePayload()
    data class Playlist(val playlistId: String) : SharePayload()
    data class Artist(val artistId: String) : SharePayload()
}
