package com.aeswox.arcmusic.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- Last.fm ---

interface LastFmService {
    @GET("2.0/?method=artist.getinfo&format=json")
    suspend fun getArtistInfo(
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String = ApiKeys.LAST_FM_API_KEY
    ): LastFmResponse
}

@JsonClass(generateAdapter = true)
data class LastFmResponse(
    val artist: LastFmArtist?
)

@JsonClass(generateAdapter = true)
data class LastFmArtist(
    val image: List<LastFmImage>?,
    val bio: LastFmBio?
)

@JsonClass(generateAdapter = true)
data class LastFmBio(
    val summary: String?,
    val content: String?
)

@JsonClass(generateAdapter = true)
data class LastFmImage(
    @Json(name = "#text") val text: String,
    val size: String
)

// --- TheAudioDB ---

interface TheAudioDbService {
    @GET("api/v1/json/{apiKey}/search.php")
    suspend fun searchArtist(
        @Path("apiKey") apiKey: String = ApiKeys.THE_AUDIO_DB_API_KEY,
        @Query("s") artist: String
    ): TheAudioDbResponse

    @GET("api/v1/json/{apiKey}/artist-mb.php")
    suspend fun searchArtistByMbid(
        @Path("apiKey") apiKey: String = ApiKeys.THE_AUDIO_DB_API_KEY,
        @Query("i") mbid: String
    ): TheAudioDbResponse
}

@JsonClass(generateAdapter = true)
data class TheAudioDbResponse(
    val artists: List<TheAudioDbArtist>?
)

@JsonClass(generateAdapter = true)
data class TheAudioDbArtist(
    val strArtistThumb: String?,
    val strBiographyEN: String?
)

// --- MusicBrainz ---

interface MusicBrainzService {
    @GET("ws/2/artist/")
    suspend fun searchArtist(
        @Query("query") query: String,
        @Query("fmt") format: String = "json"
    ): MusicBrainzResponse
}

@JsonClass(generateAdapter = true)
data class MusicBrainzResponse(
    val artists: List<MusicBrainzArtist>?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzArtist(
    val id: String,
    val name: String,
    val relations: List<MusicBrainzRelation>? = null
)

@JsonClass(generateAdapter = true)
data class MusicBrainzRelation(
    val type: String,
    val url: MusicBrainzUrl?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzUrl(
    val resource: String
)
