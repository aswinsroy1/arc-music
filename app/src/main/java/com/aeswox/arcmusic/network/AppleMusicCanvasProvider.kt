package com.aeswox.arcmusic.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

data class CanvasArtwork(
    val animated: String? = null,
    val videoUrl: String? = null,
) {
    val preferredUrl: String?
        get() = animated ?: videoUrl
}

@Singleton
class AppleMusicCanvasProvider @Inject constructor() {

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // ── Token cache ───────────────────────────────────────────────────────────
    private var cachedToken: String? = null
    private var tokenExpiryMs: Long = 0L

    // ── Result cache ──────────────────────────────────────────────────────────
    private data class CacheEntry(val url: String?, val expiresAtMs: Long)
    private val resultCache = ConcurrentHashMap<String, CacheEntry>()
    private val CACHE_TTL_MS = 1000L * 60 * 60 * 24 // 24 hours

    // ── Public API ────────────────────────────────────────────────────────────

    suspend fun getCanvasUrl(title: String, artist: String, album: String? = null): String? =
        withContext(Dispatchers.IO) {
            val key = "${title.trim().lowercase()}|${artist.trim().lowercase()}"
            resultCache[key]?.let { if (it.expiresAtMs > System.currentTimeMillis()) return@withContext it.url }

            // 1) Search songs
            val result = searchAndFetchMotion(term = title, artist = artist, album = album, type = "songs")
                // 2) If not found, try album search fallback
                ?: if (!album.isNullOrBlank()) searchAndFetchMotion(term = album, artist = artist, album = null, type = "albums") else null

            val url = result?.preferredUrl
            resultCache[key] = CacheEntry(url, System.currentTimeMillis() + CACHE_TTL_MS)
            url
        }

    // ── Token scraping ────────────────────────────────────────────────────────

    private suspend fun getOrFetchToken(): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiryMs - 60_000) return@withContext cachedToken!!
        try {
            val html = client.newCall(
                Request.Builder().url("https://music.apple.com/us/browse")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()
            ).execute().use { r -> if (!r.isSuccessful) return@use null; r.body?.string() }
                ?: return@withContext cachedToken ?: ""

            val scriptRegex = Pattern.compile("/assets/index(?:-legacy)?[~-][a-zA-Z0-9_-]+\\.js")
            val matcher = scriptRegex.matcher(html)
            val scripts = mutableListOf<String>()
            while (matcher.find()) { val s = matcher.group(); if (!scripts.contains(s)) scripts.add(s) }

            var fetchedToken: String? = null
            for (scriptPath in scripts) {
                val scriptText = client.newCall(
                    Request.Builder().url("https://music.apple.com$scriptPath")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build()
                ).execute().use { r -> if (!r.isSuccessful) return@use null; r.body?.string() } ?: continue

                val tMatcher = Pattern.compile("ey[a-zA-Z0-9_-]+\\.ey[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+").matcher(scriptText)
                while (tMatcher.find()) {
                    val token = tMatcher.group()
                    try {
                        val parts = token.split(".")
                        if (parts.size >= 2) {
                            val decoded = String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
                            if (decoded.contains("iss") && decoded.contains("exp")) {
                                val expIdx = decoded.indexOf("\"exp\":")
                                if (expIdx != -1) {
                                    val expSec = decoded.substring(expIdx + 6).takeWhile { it.isDigit() }.toLongOrNull() ?: 0L
                                    if (expSec * 1000 > now) { fetchedToken = token; tokenExpiryMs = expSec * 1000; break }
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                if (fetchedToken != null) break
            }
            if (fetchedToken != null) cachedToken = fetchedToken
            cachedToken ?: ""
        } catch (_: Exception) { cachedToken ?: "" }
    }

    // ── Catalog search ────────────────────────────────────────────────────────

    private suspend fun searchAndFetchMotion(
        term: String,
        artist: String,
        album: String?,
        type: String,
    ): CanvasArtwork? = withContext(Dispatchers.IO) {
        try {
            val token = getOrFetchToken()
            if (token.isBlank()) return@withContext null

            val query = if (term.contains(artist, ignoreCase = true)) term else "$artist $term"
            val url = buildUrl("https://amp-api.music.apple.com/v1/catalog/us/search",
                "term" to query,
                "types" to type,
                "limit" to "10",
                "extend" to "editorialVideo",
                "include" to "albums"
            )

            val bodyStr = client.newCall(
                Request.Builder().url(url)
                    .header("Authorization", "Bearer $token")
                    .header("Origin", "https://music.apple.com")
                    .header("Referer", "https://music.apple.com/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()
            ).execute().use { r -> if (r.code != 200) return@withContext null; r.body?.string() }
                ?: return@withContext null

            val root = JSONObject(bodyStr)
            val dataArray: JSONArray = root.optJSONObject("results")?.optJSONObject(type)
                ?.optJSONArray("data") ?: return@withContext null

            // Score all candidates, pick the best one
            data class Scored(val score: Int, val obj: JSONObject)
            val scored = mutableListOf<Scored>()

            for (i in 0 until dataArray.length()) {
                val obj = dataArray.getJSONObject(i)
                val attrs = obj.optJSONObject("attributes") ?: continue
                val resultArtist = attrs.optString("artistName")
                val resultName = attrs.optString("name")
                val resultCollection = attrs.optString("albumName").ifBlank { attrs.optString("collectionName") }

                // Artist must match
                if (!artistMatches(artist, resultArtist)) continue

                // Blacklist
                val nl = resultName.lowercase(Locale.ROOT)
                val cl = resultCollection.lowercase(Locale.ROOT)
                if (nl.contains("playlist") || nl.contains("essentials") || nl.contains("session") ||
                    cl.contains("playlist") || cl.contains("essentials") || cl.contains("session") ||
                    cl.contains("dj mix") || cl.contains("apple music") || cl.contains("today's hits")) continue

                var score = 10
                when {
                    resultName.equals(term, ignoreCase = true) -> score += 15
                    resultName.contains(term, ignoreCase = true) || term.contains(resultName, ignoreCase = true) -> score += 7
                    else -> score -= 10
                }
                if (!album.isNullOrBlank()) {
                    when {
                        resultCollection.equals(album, ignoreCase = true) -> score += 20
                        resultCollection.contains(album, ignoreCase = true) || album.contains(resultCollection, ignoreCase = true) -> score += 10
                    }
                }
                scored.add(Scored(score, obj))
            }

            scored.sortByDescending { it.score }

            for ((score, obj) in scored) {
                if (score < 12) continue
                val attrs = obj.optJSONObject("attributes") ?: continue
                val objType = obj.optString("type")

                // Check direct editorialVideo first
                val ev = attrs.optJSONObject("editorialVideo")
                if (ev != null) {
                    val hlsUrl = extractEditorialVideoUrl(ev)
                    if (!hlsUrl.isNullOrBlank()) return@withContext CanvasArtwork(animated = hlsUrl)
                }

                // Resolve albumId and do full lookup
                var albumId: String? = null
                if (objType == "songs") {
                    albumId = obj.optJSONObject("relationships")?.optJSONObject("albums")
                        ?.optJSONArray("data")?.optJSONObject(0)?.optString("id")
                    if (albumId.isNullOrBlank()) albumId = attrs.optString("collectionId").takeIf { it.isNotBlank() }
                    if (albumId.isNullOrBlank()) {
                        val urlStr = attrs.optString("url")
                        val afterAlbum = urlStr.substringAfter("/album/", "").substringBefore("?")
                        val id = afterAlbum.substringAfterLast("/", "")
                        if (id.isNotEmpty() && id.all { it.isDigit() }) albumId = id
                    }
                } else if (objType == "albums") {
                    albumId = obj.optString("id").takeIf { it.isNotBlank() }
                }

                if (albumId.isNullOrBlank() || albumId!!.startsWith("pl.")) continue
                val fetched = fetchMotionArtwork(albumId, token)
                if (fetched != null) return@withContext fetched
            }
            null
        } catch (_: Exception) { null }
    }

    private suspend fun fetchMotionArtwork(albumId: String, token: String): CanvasArtwork? =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://amp-api.music.apple.com/v1/catalog/us/albums/$albumId?extend=editorialVideo&include=tracks"
                val bodyStr = client.newCall(
                    Request.Builder().url(url)
                        .header("Authorization", "Bearer $token")
                        .header("Origin", "https://music.apple.com")
                        .header("Referer", "https://music.apple.com/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build()
                ).execute().use { r -> if (r.code != 200) return@withContext null; r.body?.string() }
                    ?: return@withContext null

                val root = JSONObject(bodyStr)
                val albumObj = root.optJSONArray("data")?.optJSONObject(0) ?: return@withContext null
                val attrs = albumObj.optJSONObject("attributes") ?: return@withContext null
                val albumName = attrs.optString("name").lowercase(Locale.ROOT)
                if (albumName.contains("playlist") || albumName.contains("essentials") ||
                    albumName.contains("dj mix") || albumName.contains("apple music")) return@withContext null

                val ev = attrs.optJSONObject("editorialVideo") ?: return@withContext null
                val hlsUrl = extractEditorialVideoUrl(ev) ?: return@withContext null
                CanvasArtwork(animated = hlsUrl)
            } catch (_: Exception) { null }
        }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Matches Rhythm's exact field priority:
     * motionDetailRaw → motionDetailSquare → motionDetailTall → motionDetailStatic
     * Value is a DIRECT string field (video / videoUrl / hlsUrl / url) — NOT a nested object.
     */
    private fun extractEditorialVideoUrl(ev: JSONObject): String? {
        val candidates = listOf("motionDetailRaw", "motionDetailSquare", "motionDetailTall", "motionDetailStatic")
        for (key in candidates) {
            val asset = ev.optJSONObject(key) ?: continue
            val video = asset.optString("video").takeIf { it.isNotBlank() }
                ?: asset.optString("videoUrl").takeIf { it.isNotBlank() }
                ?: asset.optString("hlsUrl").takeIf { it.isNotBlank() }
                ?: asset.optString("url").takeIf { it.isNotBlank() }
            if (!video.isNullOrBlank()) return video
        }
        return null
    }

    private fun artistMatches(requested: String, returned: String): Boolean {
        val delimiters = Regex("(?:\\s*,\\s*|\\s*&\\s*|\\s+×\\s+|\\s+x\\s+|\\bfeat\\.?\\b|\\bft\\.?\\b|\\bfeaturing\\b|\\bwith\\b)", RegexOption.IGNORE_CASE)
        fun split(s: String) = s.split(delimiters).map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotBlank() }
        val reqList = split(requested)
        val retList = split(returned)
        if (reqList.isEmpty() || retList.isEmpty()) return false
        return reqList.all { req -> retList.any { res -> res == req } }
    }

    private fun buildUrl(base: String, vararg params: Pair<String, String>): String {
        val sb = StringBuilder(base).append('?')
        params.forEach { (k, v) -> sb.append(java.net.URLEncoder.encode(k, "UTF-8")).append('=').append(java.net.URLEncoder.encode(v, "UTF-8")).append('&') }
        return sb.trimEnd('&').toString()
    }
}
