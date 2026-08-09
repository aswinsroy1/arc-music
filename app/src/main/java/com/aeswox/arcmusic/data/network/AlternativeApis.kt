package com.aeswox.arcmusic.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

// --- Last.fm ---

interface LastFmService {
    @GET("2.0/?method=artist.getinfo&format=json")
    suspend fun getArtistInfo(
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String = ApiKeys.LAST_FM_API_KEY
    ): LastFmResponse

    @GET("2.0/?method=artist.getsimilar&format=json")
    suspend fun getArtistSimilar(
        @Query("artist") artist: String,
        @Query("limit") limit: Int = 10,
        @Query("api_key") apiKey: String = ApiKeys.LAST_FM_API_KEY
    ): LastFmSimilarResponse

    @GET("2.0/?method=artist.gettoptags&format=json")
    suspend fun getArtistTopTags(
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String = ApiKeys.LAST_FM_API_KEY
    ): LastFmTagsResponse

    @GET("2.0/?method=chart.gettoptracks&format=json")
    suspend fun getChartTopTracks(
        @Query("limit") limit: Int = 50,
        @Query("api_key") apiKey: String = ApiKeys.LAST_FM_API_KEY
    ): LastFmChartResponse
}

@JsonClass(generateAdapter = true)
data class LastFmResponse(
    val artist: LastFmArtist?
)

@JsonClass(generateAdapter = true)
data class LastFmArtist(
    val name: String? = null,
    val mbid: String? = null,
    val image: List<LastFmImage>? = null,
    val bio: LastFmBio? = null
)

@JsonClass(generateAdapter = true)
data class LastFmBio(
    val summary: String?,
    val content: String?
)

@JsonClass(generateAdapter = true)
data class LastFmImage(
    @Json(name = "#text") val text: String? = null,
    val size: String? = null
)

@JsonClass(generateAdapter = true)
data class LastFmSimilarResponse(
    val similarartists: LastFmSimilarArtists?
)

@JsonClass(generateAdapter = true)
data class LastFmSimilarArtists(
    val artist: List<LastFmArtist>?
)

@JsonClass(generateAdapter = true)
data class LastFmTagsResponse(
    val toptags: LastFmTopTags?
)

@JsonClass(generateAdapter = true)
data class LastFmTopTags(
    val tag: List<LastFmTag>?
)

@JsonClass(generateAdapter = true)
data class LastFmTag(
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class LastFmChartResponse(
    val tracks: LastFmChartTracks?
)

@JsonClass(generateAdapter = true)
data class LastFmChartTracks(
    val track: List<LastFmChartTrack>?
)

@JsonClass(generateAdapter = true)
data class LastFmChartTrack(
    val name: String? = null,
    val artist: LastFmArtist? = null,
    val image: List<LastFmImage>? = null
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

    @GET("api/v1/json/{apiKey}/searchalbum.php")
    suspend fun searchAlbum(
        @Path("apiKey") apiKey: String = ApiKeys.THE_AUDIO_DB_API_KEY,
        @Query("s") artist: String,
        @Query("a") album: String
    ): TheAudioDbAlbumResponse
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

@JsonClass(generateAdapter = true)
data class TheAudioDbAlbumResponse(
    val album: List<TheAudioDbAlbum>?
)

@JsonClass(generateAdapter = true)
data class TheAudioDbAlbum(
    val strAlbumThumb: String?
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

    @GET("ws/2/release-group")
    suspend fun searchReleaseGroups(
        @Query("query") query: String,
        @Query("fmt") format: String = "json",
        @Query("limit") limit: Int = 100
    ): MusicBrainzReleaseGroupResponse

    @GET("ws/2/artist/{mbid}")
    suspend fun getArtistById(
        @Path("mbid") mbid: String,
        @Query("inc") include: String = "url-rels",
        @Query("fmt") format: String = "json"
    ): MusicBrainzArtist



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
data class MusicBrainzReleaseGroupResponse(
    @Json(name = "release-groups") val releaseGroups: List<MusicBrainzReleaseGroup>?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzRecordingResponse(
    val recordings: List<MusicBrainzRecording>?
)

@JsonClass(generateAdapter = true)
data class MusicBrainzRecording(
    val id: String,
    val title: String,
    @Json(name = "first-release-date") val firstReleaseDate: String? = null,
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
    @Json(name = "first-release-date") val firstReleaseDate: String? = null,
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

// --- Wikipedia ---

interface WikipediaService {
    @GET
    suspend fun getSummary(@Url url: String): WikipediaSummaryResponse
}

@JsonClass(generateAdapter = true)
data class WikipediaSummaryResponse(
    val extract: String?,
    val originalimage: WikipediaImage?
)

@JsonClass(generateAdapter = true)
data class WikipediaImage(
    val source: String?
)

interface ItunesService {
    @GET("search")
    suspend fun searchAlbum(
        @Query("term") term: String,
        @Query("entity") entity: String = "album",
        @Query("limit") limit: Int = 100
    ): ItunesSearchResponse

    @GET("search")
    suspend fun searchTrack(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 100
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
    val collectionName: String?,
    val trackName: String?,
    val artworkUrl100: String?,
    val collectionViewUrl: String?,
    val trackViewUrl: String?
)

// --- Odesli (Songlink) ---

interface OdesliService {
    @GET("v1-alpha.1/links")
    suspend fun getLinks(
        @Query("url") url: String
    ): OdesliResponse
}

@JsonClass(generateAdapter = true)
data class OdesliResponse(
    val entityUniqueId: String,
    val linksByPlatform: Map<String, OdesliPlatformLink>?
)

@JsonClass(generateAdapter = true)
data class OdesliPlatformLink(
    val url: String
)

// --- Fanart.tv ---

interface FanartTvService {
    @GET("v3/music/{mbid}")
    suspend fun getArtistImages(
        @Path("mbid") mbid: String,
        @Query("api_key") apiKey: String
    ): FanartTvResponse
}

@JsonClass(generateAdapter = true)
data class FanartTvResponse(
    val name: String? = null,
    val mbid_id: String? = null,
    val artistthumb: List<FanartTvImage>? = null
)

@JsonClass(generateAdapter = true)
data class FanartTvImage(
    val id: String? = null,
    val url: String? = null,
    val likes: String? = null
)
