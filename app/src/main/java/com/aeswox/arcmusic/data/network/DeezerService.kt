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

    @GET("album/{id}")
    suspend fun getAlbumDetails(@retrofit2.http.Path("id") id: Long): DeezerAlbumDetail
}

@JsonClass(generateAdapter = true)
data class DeezerSearchResponse<T>(
    @Json(name = "data") val data: List<T>
)

@JsonClass(generateAdapter = true)
data class DeezerArtist(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "link") val link: String? = null,
    @Json(name = "picture_xl") val pictureXl: String? = null
)

@JsonClass(generateAdapter = true)
data class DeezerAlbum(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "link") val link: String? = null,
    @Json(name = "cover_xl") val coverXl: String? = null
)

@JsonClass(generateAdapter = true)
data class DeezerTrack(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "link") val link: String? = null,
    @Json(name = "album") val album: DeezerAlbum? = null,
    @Json(name = "artist") val artist: DeezerArtist? = null,
    @Json(name = "track_position") val trackPosition: Int? = null,
    @Json(name = "disk_number") val diskNumber: Int? = null,
    @Json(name = "explicit_lyrics") val explicitLyrics: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class DeezerAlbumDetail(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "genres") val genres: DeezerGenresList? = null
)

@JsonClass(generateAdapter = true)
data class DeezerGenresList(
    @Json(name = "data") val data: List<DeezerGenre>
)

@JsonClass(generateAdapter = true)
data class DeezerGenre(
    @Json(name = "name") val name: String
)
