package com.aeswox.arcmusic.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Top-level response from the LyricsPlus API.
 *
 * When [type] == "Word", each [LyricsPlusLine] in [lyrics] will contain a non-null
 * [LyricsPlusLine.syllabus] list with word/syllable-level timestamps (in ms).
 *
 * When [type] == "Line", [LyricsPlusLine.syllabus] will be null or absent, and only
 * line-level sync timestamps are available — graceful degradation to line display.
 *
 * All timestamps ([LyricsPlusLine.time], [LyricsPlusSyllable.time]) are in milliseconds.
 */
@JsonClass(generateAdapter = true)
data class LyricsPlusResponse(
    @Json(name = "type") val type: String?,          // "Word" or "Line"
    @Json(name = "lyrics") val lyrics: List<LyricsPlusLine>?
)

@JsonClass(generateAdapter = true)
data class LyricsPlusLine(
    /** Absolute start time of this line in milliseconds. */
    @Json(name = "time") val time: Long,
    /** Plain text of the full line. */
    @Json(name = "text") val text: String?,
    /** Word/syllable-level segments. Null when type == "Line". */
    @Json(name = "syllabus") val syllabus: List<LyricsPlusSyllable>?
)

@JsonClass(generateAdapter = true)
data class LyricsPlusSyllable(
    /** Absolute start time of this syllable/word in milliseconds. */
    @Json(name = "time") val time: Long,
    /** The syllable or word text (may include trailing space to indicate word boundary). */
    @Json(name = "text") val text: String?
)
