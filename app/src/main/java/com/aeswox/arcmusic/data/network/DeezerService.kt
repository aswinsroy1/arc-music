package com.aeswox.arcmusic.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface DeezerService {
    @GET("search/artist")
    suspend fun searchArtist(@Query("q") query: String): DeezerSearchResponse<DeezerArtist>

    @GET("search/album")
    suspend fun searchAlbum(@Query("q") query: String): DeezerSearchResponse<DeezerAlbum>

    @GET("search/track")
    suspend fun searchTrack(@Query("q") query: String): DeezerSearchResponse<DeezerTrack>
}

@JsonClass(generateAdapter = true)
data class DeezerSearchResponse<T>(
    @Json(name = "data") val data: List<T>
)

@JsonClass(generateAdapter = true)
data class DeezerArtist(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "picture_xl") val pictureXl: String?
)

@JsonClass(generateAdapter = true)
data class DeezerAlbum(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "cover_xl") val coverXl: String?
)

@JsonClass(generateAdapter = true)
data class DeezerTrack(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "album") val album: DeezerAlbum?,
    @Json(name = "artist") val artist: DeezerArtist?
)
