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

    private var totpSecret: ByteArray? = null
    private var totpVersion: String = "19"
    private var lastSecretFetchTime: Long = 0L

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

        awaitTotpSecrets()

        val serverTime = fetchServerTime(spDcCookie)
        val localTime = System.currentTimeMillis()

        val totpLocal = generateTOTP(localTime, totpSecret!!)
        val totpServer = generateTOTP((serverTime / 30) * 30000, totpSecret!!)

        val url = "https://open.spotify.com/api/token?reason=init&productType=mobile-web-player&totp=$totpLocal&totpVer=$totpVersion&totpServer=$totpServer"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Origin", "https://open.spotify.com/")
            .header("Referer", "https://open.spotify.com/")
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

    private suspend fun fetchServerTime(spDcCookie: String): Long {
        val request = Request.Builder()
            .url("https://open.spotify.com/api/server-time")
            .header("User-Agent", userAgent)
            .header("Origin", "https://open.spotify.com/")
            .header("Referer", "https://open.spotify.com/")
            .header("Cookie", "sp_dc=$spDcCookie")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return System.currentTimeMillis()
                val body = response.body?.string() ?: return System.currentTimeMillis()
                val json = JSONObject(body)
                val timeSec = json.optLong("serverTime", 0L)
                if (timeSec > 0) timeSec * 1000 else System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private suspend fun awaitTotpSecrets() {
        val now = System.currentTimeMillis()
        if (totpSecret != null && now - lastSecretFetchTime < 3600_000) return

        val secretsUrl = "https://raw.githubusercontent.com/xyloflake/spot-secrets-go/refs/heads/main/secrets/secretDict.json"
        val request = Request.Builder().url(secretsUrl).build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    var maxVersion = 0
                    for (key in json.keys()) {
                        val ver = key.toIntOrNull() ?: 0
                        if (ver > maxVersion) maxVersion = ver
                    }
                    if (maxVersion > 0) {
                        totpVersion = maxVersion.toString()
                        val arr = json.getJSONArray(totpVersion)
                        totpSecret = decodeTotpSecret(arr)
                        lastSecretFetchTime = now
                        return
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SpotifyCanvas", "Failed to fetch TOTP secrets", e)
        }

        // Fallback
        if (totpSecret == null) {
            val fallbackData = intArrayOf(99, 111, 47, 88, 49, 56, 118, 65, 52, 67, 50, 104, 117, 101, 55, 94, 95, 75, 94, 49, 69, 36, 85, 64, 74, 60)
            totpSecret = decodeTotpSecret(fallbackData)
            totpVersion = "19"
        }
    }

    private fun decodeTotpSecret(arr: JSONArray): ByteArray {
        val ints = IntArray(arr.length())
        for (i in 0 until arr.length()) {
            ints[i] = arr.getInt(i)
        }
        return decodeTotpSecret(ints)
    }

    private fun decodeTotpSecret(arr: IntArray): ByteArray {
        val sb = StringBuilder()
        for (i in arr.indices) {
            val v = arr[i] xor ((i % 33) + 9)
            sb.append(v.toChar())
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun generateTOTP(timeMs: Long, secret: ByteArray): String {
        val timeStep = timeMs / 30000
        val data = ByteBuffer.allocate(8).putLong(timeStep).array()
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(secret, "HmacSHA1"))
        val hash = mac.doFinal(data)
        
        val offset = hash[hash.size - 1].toInt() and 0xF
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                     ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                     ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                     (hash[offset + 3].toInt() and 0xFF)
                     
        val otp = binary % 1000000
        return String.format("%06d", otp)
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
