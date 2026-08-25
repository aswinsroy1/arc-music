package com.aeswox.arcmusic.data.model

/**
 * Controls how lyric lines are rendered on the full-screen lyrics overlay.
 *
 * [FADE]          – New default. Inactive lines are dim (opacity only, zero blur on text).
 *                   Active line uses cumulative word-fill: every word whose timestamp
 *                   has been reached stays bright, filling left-to-right as the song plays.
 *
 * [DISTANCE_BLUR] – Original style. Inactive lines are progressively blurred based on
 *                   their distance from the active line. Word highlighting is spotlight-only
 *                   (single active word bright at a time).
 */
enum class LyricsDisplayStyle {
    FADE,
    DISTANCE_BLUR
}
