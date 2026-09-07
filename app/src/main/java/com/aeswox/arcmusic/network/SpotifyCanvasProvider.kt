package com.aeswox.arcmusic.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyCanvasProvider @Inject constructor() {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private var cachedToken: String? = null
    private var tokenExpiryMs: Long = 0L

    private val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"

    suspend fun getCanvasUrl(title: String, artist: String, spDcCookie: String): String? = withContext(Dispatchers.IO) {
        if (spDcCookie.isBlank()) {
            Log.w("SpotifyCanvas", "No SP_DC cookie provided.")
            return@withContext null
        }
        
        try {
            val token = getAccessToken(spDcCookie) ?: return@withContext null
            val trackId = resolveTrackId(title, artist, token) ?: return@withContext null
            return@withContext fetchCanvas("spotify:track:$trackId", token)
        } catch (e: Exception) {
            Log.e("SpotifyCanvas", "Error fetching canvas for $title - $artist", e)
            null
        }
    }

    private suspend fun getAccessToken(spDcCookie: String): String? {
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiryMs) {
            return cachedToken
        }

        val url = "https://open.spotify.com/get_access_token?reason=transport&productType=web_player"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Origin", "https://open.spotify.com")
            .header("Referer", "https://open.spotify.com/")
            .header("App-Platform", "WebPlayer")
            .header("Cookie", "sp_dc=$spDcCookie")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val token = json.getString("accessToken")
                val exp = json.optLong("accessTokenExpirationTimestampMs", now + 3600_000)
                cachedToken = token
                tokenExpiryMs = exp
                token
            }
        } catch (e: Exception) {
            Log.e("SpotifyCanvas", "Failed to fetch access token", e)
            null
        }
    }

    private suspend fun resolveTrackId(title: String, artist: String, token: String): String? {
        val query = "track:${title} artist:${artist}"
        val url = "https://api.spotify.com/v1/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&type=track&limit=1"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val items = json.optJSONObject("tracks")?.optJSONArray("items")
                if (items != null && items.length() > 0) {
                    items.getJSONObject(0).getString("id")
                } else null
            }
        } catch (e: Exception) {
            Log.e("SpotifyCanvas", "Failed to resolve track id", e)
            null
        }
    }

    private suspend fun fetchCanvas(trackUri: String, token: String): String? {
        val payload = encodeCanvasRequest(trackUri)
        
        val request = Request.Builder()
            .url("https://spclient.wg.spotify.com/canvaz-cache/v0/canvases")
            .post(payload.toRequestBody(null))
            .header("Accept", "application/protobuf")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", "Bearer $token")
            .header("User-Agent", "Spotify/9.0.34.593 iOS/18.4 (iPhone15,3)")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.bytes() ?: return null
                extractCanvasUrl(body)
            }
        } catch (e: Exception) {
            Log.e("SpotifyCanvas", "Failed to fetch canvas protobuf", e)
            null
        }
    }

    private fun encodeCanvasRequest(trackUri: String): ByteArray {
        val uriBytes = trackUri.toByteArray(Charsets.UTF_8)
        
        // Track message: tag 1, type 2 (string). track_uri is tag 1 inside Track.
        val trackMsgBytes = ByteArrayOutputStream()
        trackMsgBytes.write((1 shl 3) or 2) // tag 1, length-delimited
        trackMsgBytes.write(uriBytes.size)
        trackMsgBytes.write(uriBytes)
        
        val trackBytes = trackMsgBytes.toByteArray()
        
        // CanvasRequest message: tag 1, type 2 (repeated Track)
        val requestBytes = ByteArrayOutputStream()
        requestBytes.write((1 shl 3) or 2)
        requestBytes.write(trackBytes.size)
        requestBytes.write(trackBytes)
        
        return requestBytes.toByteArray()
    }

    private fun extractCanvasUrl(protobuf: ByteArray): String? {
        // Manually scan the protobuf byte array for a string that starts with "http" and ends with ".mp4" or ".jpg"
        val str = String(protobuf, Charsets.UTF_8)
        val regex = Regex("https://[^\\s\"'<>\\x00]+\\.(?:mp4|jpg)")
        val match = regex.find(str)
        return match?.value
    }
}
