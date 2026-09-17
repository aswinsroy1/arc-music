package com.aeswox.arcmusic.data

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

// ─── ARC SCAN DIAGNOSTICS ────────────────────────────────────────────────────
// Tag for all scan-pipeline diagnostic logs.
// To watch on device: adb logcat -s ARC_SCAN_DIAG
private const val TAG = "ARC_SCAN_DIAG"

/**
 * MediaStore-backed audio scanner.
 *
 * Key design decisions (mirroring PixelPlayer's stable approach):
 *
 * 1. NO IS_MUSIC filter — Motorola/OEM ROMs on Android 16 leave valid music
 *    tracks flagged as non-music (IS_MUSIC=0), making them invisible with the
 *    traditional filter. We use DURATION + non-blank TITLE instead.
 *
 * 2. NO File.exists() check — Android scoped storage (API 29+) can return false
 *    for valid files accessible via ContentResolver. This check was causing tracks
 *    to be silently dropped on Motorola devices.
 *
 * 3. Incremental scanning — pass sinceTimestampSec > 0 to only retrieve tracks
 *    added or modified after that point.
 *
 * 4. Proper genre loading — API 30+ reads GENRE column directly from the main
 *    cursor; below API 30 queries the Audio.Genres table with parallel fan-out
 *    (Semaphore(4)) and a 1-hour in-memory cache.
 */
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanLogger: ScanLogger
) {

    companion object {
        private const val GENRE_CACHE_TTL_MS = 60 * 60 * 1000L // 1 hour

        @Volatile private var genreMapCache: Map<Long, String> = emptyMap()
        @Volatile private var genreMapCacheTimestamp: Long = 0L

        fun invalidateGenreCache() {
            genreMapCache = emptyMap()
            genreMapCacheTimestamp = 0L
        }
    }

    /**
     * Scan audio files from MediaStore.
     *
     * @param targetFolder   If non-null, only files under this folder path are returned.
     * @param sinceTimestampSec  Epoch seconds. When > 0, only files with DATE_MODIFIED or
     *                           DATE_ADDED newer than this value are returned (incremental mode).
     * @param minDurationMs  Minimum track duration in milliseconds. 0 = no filter.
     * @param minBitrateKbps Minimum bitrate in kbps. 0 = no filter.
     */
    suspend fun scanAudioFiles(
        targetFolder: String? = null,
        sinceTimestampSec: Long = 0L,
        minDurationMs: Long = 0L,
        minBitrateKbps: Int = 0
    ): List<ScannedTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<ScannedTrack>()
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val nowMs = System.currentTimeMillis()
        val nowSec = nowMs / 1000L
        scanLogger.i("╔══ scanAudioFiles() ENTRY ══════════════════════════════════")
        scanLogger.i("║  targetFolder = $targetFolder")
        scanLogger.i("║  sinceTimestampSec = $sinceTimestampSec  (0 = full scan)")
        scanLogger.i("║  minDurationMs = $minDurationMs  |  minBitrateKbps = $minBitrateKbps")
        scanLogger.i("║  System.currentTimeMillis() = $nowMs  (ms since epoch)")
        scanLogger.i("║  now_sec = $nowSec")
        scanLogger.i("╚════════════════════════════════════════════════════════════")

        // Load genre map upfront — parallel for pre-API30, instant from cache on repeat calls
        val genreMap = loadGenreMap()

        // Build projection — add GENRE column only on API 30+
        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.YEAR
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionList.add(MediaStore.Audio.Media.GENRE)
        }
        val projection = projectionList.toTypedArray()

        // ── Selection ──────────────────────────────────────────────────────────
        // We intentionally do NOT use IS_MUSIC != 0 — Motorola/OEM ROMs leave
        // valid tracks flagged as non-music, making them invisible.
        val selectionBuilder = StringBuilder()
        val selectionArgs = mutableListOf<String>()

        // Duration filter
        val clampedMin = minDurationMs.coerceAtLeast(0L)
        selectionBuilder.append("(${MediaStore.Audio.Media.DURATION} >= ?")
        selectionArgs.add(clampedMin.toString())
        // Allow 0-duration tracks through too — some Dolby Atmos files report 0 initially
        selectionBuilder.append(" OR ${MediaStore.Audio.Media.DURATION} = 0)")

        // Non-blank title
        selectionBuilder.append(" AND COALESCE(${MediaStore.Audio.Media.TITLE}, '') != ''")
        // Non-null path
        selectionBuilder.append(" AND ${MediaStore.Audio.Media.DATA} IS NOT NULL")

        // Folder filter
        if (targetFolder != null) {
            selectionBuilder.append(" AND ${MediaStore.Audio.Media.DATA} LIKE ?")
            selectionArgs.add("$targetFolder%")
        }

        // Incremental filter
        if (sinceTimestampSec > 0L) {
            selectionBuilder.append(
                " AND (${MediaStore.Audio.Media.DATE_MODIFIED} > ? OR ${MediaStore.Audio.Media.DATE_ADDED} > ?)"
            )
            selectionArgs.add(sinceTimestampSec.toString())
            selectionArgs.add(sinceTimestampSec.toString())
        }

        val selection = selectionBuilder.toString()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs.toTypedArray(),
            null
        )?.use { cursor ->
            val idColumn          = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn       = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn      = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn       = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumArtistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
            val albumIdColumn     = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val trackColumn       = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val durationColumn    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn        = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val sizeColumn        = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModColumn     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val mimeTypeColumn    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val bitrateColumn     = cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
            val yearColumn        = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val genreColumn       = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
            } else -1

            val seenPaths = mutableSetOf<String>()
            var diagTotal         = 0
            var diagDroppedDupe   = 0
            var diagDroppedBitrate = 0

            while (cursor.moveToNext()) {
                diagTotal++

                val filePath = cursor.getString(dataColumn) ?: continue
                if (filePath.isBlank()) continue

                // Dedup by path
                if (!seenPaths.add(filePath)) {
                    diagDroppedDupe++
                    continue
                }

                val id        = cursor.getLong(idColumn)
                val title     = cursor.getString(titleColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Title"
                val artist    = cursor.getString(artistColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
                val album     = cursor.getString(albumColumn)?.takeIf { it.isNotBlank() } ?: "Unknown Album"
                val albumId   = if (albumIdColumn >= 0) cursor.getLong(albumIdColumn) else null
                val albumArtist = if (albumArtistColumn >= 0) {
                    cursor.getString(albumArtistColumn)?.takeIf { it.isNotBlank() } ?: artist
                } else artist

                // Track/disc number — some formats encode disc as thousands (e.g. 1004 = disc 1, track 4)
                val fullTrackNumber = cursor.getInt(trackColumn)
                val trackNumber = fullTrackNumber % 1000
                val discNumber  = if (fullTrackNumber >= 1000) fullTrackNumber / 1000 else 1

                val durationMs      = cursor.getLong(durationColumn)
                val sizeBytes       = cursor.getLong(sizeColumn)
                // DATE_ADDED / DATE_MODIFIED from MediaStore are in SECONDS — multiply by 1000
                val dateAdded       = cursor.getLong(dateAddedColumn) * 1000L
                val dateModified    = cursor.getLong(dateModColumn) * 1000L
                val mimeType        = cursor.getString(mimeTypeColumn) ?: ""
                val year            = if (yearColumn >= 0) cursor.getInt(yearColumn).takeIf { it > 0 } else null

                val bitrateFromMs   = if (bitrateColumn >= 0) cursor.getInt(bitrateColumn) else 0
                var estimatedBitrate = bitrateFromMs
                if (estimatedBitrate <= 0 && durationMs > 0) {
                    val durationSecs = durationMs / 1000.0
                    if (durationSecs > 0) estimatedBitrate = ((sizeBytes * 8) / durationSecs).toInt()
                }

                // Bitrate filter
                if (minBitrateKbps > 0 && estimatedBitrate > 0 && estimatedBitrate < minBitrateKbps * 1000) {
                    diagDroppedBitrate++
                    continue
                }

                // Genre — API 30+: from GENRE column; below API 30: from pre-fetched genre map
                val genre = if (genreColumn >= 0) {
                    cursor.getString(genreColumn) ?: genreMap[id] ?: ""
                } else {
                    genreMap[id] ?: ""
                }

                val artworkUriStr = albumId?.let { "content://media/external/audio/albumart/$it" }

                // Explicit detection from title/path heuristic (real tag parsing done by DeepTagScanner)
                val hasExplicitHint = title.contains("[E]", ignoreCase = true) ||
                                      title.contains("(Explicit)", ignoreCase = true) ||
                                      filePath.contains("[E]", ignoreCase = true) ||
                                      filePath.contains("(Explicit)", ignoreCase = true)

                tracks.add(
                    ScannedTrack(
                        id            = id.toString(),
                        title         = title,
                        artist        = artist,
                        albumArtist   = albumArtist,
                        albumId       = albumId,
                        album         = album,
                        genre         = genre,
                        year          = year,
                        trackNumber   = trackNumber,
                        discNumber    = discNumber,
                        durationMs    = durationMs,
                        filePath      = filePath,
                        fileSizeBytes = sizeBytes,
                        mimeType      = mimeType,
                        bitrate       = estimatedBitrate,
                        sampleRate    = null,
                        bitDepth      = null,
                        dateAdded     = dateAdded,
                        dateModified  = dateModified,
                        artworkUri    = artworkUriStr,
                        isExplicit    = if (hasExplicitHint) true else null
                    )
                )
            }

            scanLogger.i("╔══ scanAudioFiles() SUMMARY ════════════════════════════════")
            scanLogger.i("║  Total MediaStore rows        : $diagTotal")
            scanLogger.i("║  Dropped – duplicate path     : $diagDroppedDupe")
            scanLogger.i("║  Dropped – bitrate too low    : $diagDroppedBitrate")
            scanLogger.i("║  Tracks returned to caller    : ${tracks.size}")
            scanLogger.i("║  IS_MUSIC filter              : REMOVED (Motorola fix)")
            scanLogger.i("║  File.exists() check          : REMOVED (scoped storage fix)")
            scanLogger.i("╚════════════════════════════════════════════════════════════")
        }

        tracks
    }

    /**
     * Returns all MediaStore audio IDs currently visible to the app.
     * Used by MusicRepository to detect deleted songs (DB entries with no MediaStore counterpart).
     */
    fun getAllMediaStoreIds(): Set<Long> {
        val ids = mutableSetOf<Long>()
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA)
        // Use same IS_MUSIC-free selection so we match what scanAudioFiles returns
        val selection = "COALESCE(${MediaStore.Audio.Media.TITLE}, '') != '' AND " +
                        "${MediaStore.Audio.Media.DATA} IS NOT NULL"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(idCol))
            }
        }
        return ids
    }

    fun getFoldersContainingAudio(): List<String> {
        val folders = mutableSetOf<String>()
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val selection = "COALESCE(${MediaStore.Audio.Media.TITLE}, '') != '' AND " +
                        "${MediaStore.Audio.Media.DATA} IS NOT NULL"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumn) ?: continue
                val folder = filePath.substringBeforeLast('/')
                if (folder.isNotBlank() && folder.startsWith("/")) {
                    folders.add(folder)
                }
            }
        }
        return folders.toList().sorted()
    }

    // ── Genre loading ──────────────────────────────────────────────────────────

    /**
     * Loads a map of song ID → genre name.
     *
     * On API 30+ this map is empty — the GENRE column is read directly in the
     * main cursor, which is faster and more accurate.
     *
     * On API < 30 we query the Audio.Genres table and fan out to each genre's
     * member list in parallel (Semaphore(4)). Results are cached for 1 hour to
     * avoid re-querying on every incremental scan.
     */
    private suspend fun loadGenreMap(forceRefresh: Boolean = false): Map<Long, String> = coroutineScope {
        // Skip on API 30+ — GENRE column covers it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return@coroutineScope emptyMap()

        val now = System.currentTimeMillis()
        val cacheAge = now - genreMapCacheTimestamp
        if (!forceRefresh && genreMapCache.isNotEmpty() && cacheAge < GENRE_CACHE_TTL_MS) {
            scanLogger.d("Genre map: using cache (${genreMapCache.size} entries, age ${cacheAge / 1000}s)")
            return@coroutineScope genreMapCache
        }

        val genreMap = ConcurrentHashMap<Long, String>()
        val semaphore = Semaphore(4)

        try {
            // Step 1: fetch all genres in one query
            val genres = mutableListOf<Pair<Long, String>>()
            context.contentResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME),
                null, null, null
            )?.use { cursor ->
                val idCol   = cursor.getColumnIndex(MediaStore.Audio.Genres._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME)
                if (idCol >= 0 && nameCol >= 0) {
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameCol)
                        if (!name.isNullOrBlank() && !name.equals("unknown", ignoreCase = true)) {
                            genres.add(cursor.getLong(idCol) to name)
                        }
                    }
                }
            }

            // Step 2: fan out to members for each genre, in parallel
            genres.map { (genreId, genreName) ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                        context.contentResolver.query(
                            membersUri,
                            arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
                            null, null, null
                        )?.use { mc ->
                            val audioIdCol = mc.getColumnIndex(MediaStore.Audio.Genres.Members.AUDIO_ID)
                            if (audioIdCol >= 0) {
                                while (mc.moveToNext()) {
                                    genreMap[mc.getLong(audioIdCol)] = genreName
                                }
                            }
                        }
                    }
                }
            }.awaitAll()

        } catch (e: Exception) {
            scanLogger.e("Genre map load failed: ${e.message}")
        }

        if (genreMap.isNotEmpty()) {
            genreMapCache = genreMap.toMap()
            genreMapCacheTimestamp = System.currentTimeMillis()
            scanLogger.d("Genre map: cached ${genreMap.size} entries")
        }

        genreMap
    }
}


data class ScannedTrack(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtist: String,
    val albumId: Long?,
    val album: String,
    val genre: String,
    val year: Int?,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long,
    val filePath: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val bitrate: Int,
    val sampleRate: Int?,
    val bitDepth: Int?,
    val dateAdded: Long,
    val dateModified: Long,
    val artworkUri: String?,
    val isExplicit: Boolean? = null
)
