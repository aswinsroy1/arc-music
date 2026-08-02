package com.aeswox.arcmusic.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Representa la respuesta de la API de LRCLIB.
 * Contiene la letra de la canción, tanto en formato simple como sincronizado.
 */
@JsonClass(generateAdapter = true)
data class LrcLibResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "artistName") val artistName: String,
    @Json(name = "albumName") val albumName: String,
    @Json(name = "duration") val duration: Double,
    @Json(name = "plainLyrics") val plainLyrics: String?,
    @Json(name = "syncedLyrics") val syncedLyrics: String?
)
