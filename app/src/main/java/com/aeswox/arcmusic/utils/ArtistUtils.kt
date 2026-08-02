package com.aeswox.arcmusic.utils

object ArtistUtils {
    /**
     * Splits a combined artist string into individual artist names.
     * Handles common delimiters like ",", "&", "and", "ft", "feat", "featuring".
     * E.g., "Bob, Jake feat. Sunny & Tom" -> ["Bob", "Jake", "Sunny", "Tom"]
     */
    fun splitArtists(artistString: String?): List<String> {
        if (artistString.isNullOrBlank()) return emptyList()

        // Replace all delimiter variations with a unified delimiter (e.g., "|||")
        // Use regex for case-insensitive matching and word boundaries where needed.
        var cleaned = artistString
            .replace(Regex("(?i)\\b(?:feat\\.?|ft\\.?|featuring|and)\\b"), "|||")
            .replace(Regex("[,&]"), "|||")

        // Split by the unified delimiter, trim whitespace, and filter out empty strings
        return cleaned.split("|||")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}
