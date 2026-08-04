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

    @GET("ws/2/release")
    suspend fun getReleases(
        @Query("artist") artistMbid: String,
        @Query("inc") include: String = "recordings",
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 100
    ): MusicBrainzReleaseResponse

    @GET("ws/2/release")
    suspend fun searchReleases(
        @Query("query") query: String,
        @Query("inc") include: String = "recordings+release-groups",
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 100
    ): MusicBrainzReleaseResponse

    // Lookup a single release by MBID to get inline recordings (search endpoint does NOT return tracks)
    @GET("ws/2/release/{mbid}")
    suspend fun getReleaseById(
        @Path("mbid") mbid: String,
        @Query("inc") include: String = "recordings",
        @Query("fmt") format: String = "json"
    ): MusicBrainzRelease

    @GET("ws/2/recording")
    suspend fun searchRecording(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 10
    ): MusicBrainzRecordingResponse
}

@JsonClass(generateAdapter = true)
data class MusicBrainzReleaseResponse(
    val releases: List<MusicBrainzRelease>?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzRecordingResponse(
    val recordings: List<MusicBrainzRecording>?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzRecording(
    val id: String,
    val title: String,
    @Json(name = "artist-credit") val artistCredit: List<MusicBrainzArtistCredit>?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzArtistCredit(
    val name: String,
    val artist: MusicBrainzArtist?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzRelease(
    val id: String,
    val title: String,
    val media: List<MusicBrainzMedia>?,
    @Json(name = "release-group") val releaseGroup: MusicBrainzReleaseGroup? = null
)

@JsonClass(generateAdapter = true)
data class MusicBrainzReleaseGroup(
    val id: String,
    val title: String,
    @Json(name = "primary-type") val primaryType: String?,
    @Json(name = "secondary-types") val secondaryTypes: List<String>?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzMedia(
    @Json(name = "track-count") val trackCount: Int?,
    val tracks: List<MusicBrainzTrack>? = null
)

@JsonClass(generateAdapter = true)
data class MusicBrainzTrack(
    val id: String,
    val title: String,
    val number: String? = null
)

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

interface ItunesService {
    @GET("search")
    suspend fun searchAlbum(
        @Query("term") term: String,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 10
    ): ItunesSearchResponse
}

@JsonClass(generateAdapter = true)
data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<ItunesAlbum>?
)

@JsonClass(generateAdapter = true)
data class ItunesAlbum(
    val artistName: String,
    val collectionName: String,
    val artworkUrl100: String?
)
