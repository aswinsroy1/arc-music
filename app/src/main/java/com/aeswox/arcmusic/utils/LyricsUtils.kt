package com.aeswox.arcmusic.utils

import com.aeswox.arcmusic.data.model.Lyrics
import com.aeswox.arcmusic.data.model.SyncedLine
import com.aeswox.arcmusic.data.model.SyncedWord
import java.util.regex.Pattern

object LyricsUtils {

    private val LRC_METADATA_PATTERN = Pattern.compile("^\\[[a-zA-Z]+:.*]$")
    private val LRC_LINE_REGEX = Pattern.compile("^\\[(\\d{2,}):(\\d{2})(?:[.:](\\d{2,3}))?](.*)$")
    private val LRC_WORD_TAG_REGEX = Regex("<\\d{2,}:\\d{2}[.:]\\d{2,3}>")
    private val LRC_WORD_SPLIT_REGEX = Regex("(?=<\\d{2,}:\\d{2}[.:]\\d{2,3}>)")
    private val LRC_WORD_REGEX = Pattern.compile("^<(\\d{2,}):(\\d{2})(?:[.:](\\d{2,3}))?>(.*)$")

    fun parseLyrics(lyricsText: String?): Lyrics? {
        if (lyricsText.isNullOrEmpty()) {
            return null
        }

        val syncedLines = mutableListOf<SyncedLine>()
        val plainLines = mutableListOf<String>()
        var isSynced = false

        lyricsText.lines().forEach { rawLine ->
            val line = sanitizeLrcLine(rawLine)
            if (line.isEmpty() || LRC_METADATA_PATTERN.matcher(line).matches()) return@forEach

            val lineMatcher = LRC_LINE_REGEX.matcher(line)
            if (lineMatcher.matches()) {
                isSynced = true
                val minutes = lineMatcher.group(1)?.toLong() ?: 0
                val seconds = lineMatcher.group(2)?.toLong() ?: 0
                val fraction = lineMatcher.group(3)?.toLong() ?: 0
                val textWithTags = lineMatcher.group(4)?.trim() ?: ""
                val text = stripLrcTimestamps(textWithTags)

                val millis = if (lineMatcher.group(3)?.length == 2) fraction * 10 else fraction
                val lineTimestamp = minutes * 60 * 1000 + seconds * 1000 + millis

                if (textWithTags.contains(LRC_WORD_TAG_REGEX)) {
                    val words = mutableListOf<SyncedWord>()
                    val parts = textWithTags.split(LRC_WORD_SPLIT_REGEX)
                    var pendingWordBoundary = false

                    for (part in parts) {
                        if (part.isEmpty()) continue
                        val wordMatcher = LRC_WORD_REGEX.matcher(part)
                        if (wordMatcher.find()) {
                            val wordMinutes = wordMatcher.group(1)?.toLong() ?: 0
                            val wordSeconds = wordMatcher.group(2)?.toLong() ?: 0
                            val wordFraction = wordMatcher.group(3)?.toLong() ?: 0
                            val wordTextRaw = wordMatcher.group(4) ?: ""
                            val timedWordTextRaw = wordTextRaw.substringBefore('\n').substringBefore('\r')
                            
                            val startsNewWord = words.isEmpty() || pendingWordBoundary || timedWordTextRaw.firstOrNull()?.isWhitespace() == true
                            val timedWordText = timedWordTextRaw.trim()
                            pendingWordBoundary = timedWordTextRaw.lastOrNull()?.isWhitespace() == true
                            
                            val wordMillis = if (wordMatcher.group(3)?.length == 2) wordFraction * 10 else wordFraction
                            val wordTimestamp = wordMinutes * 60 * 1000 + wordSeconds * 1000 + wordMillis
                            
                            if (timedWordText.isNotEmpty()) {
                                words.add(
                                    SyncedWord(
                                        time = wordTimestamp.toInt(),
                                        word = timedWordText,
                                        startsNewWord = startsNewWord
                                    )
                                )
                            }
                        } else {
                            val untagged = part.trim()
                            if (untagged.isNotEmpty()) {
                                words.add(
                                    SyncedWord(
                                        time = lineTimestamp.toInt(),
                                        word = untagged,
                                        startsNewWord = words.isEmpty() || pendingWordBoundary || part.firstOrNull()?.isWhitespace() == true
                                    )
                                )
                                pendingWordBoundary = part.lastOrNull()?.isWhitespace() == true
                            }
                        }
                    }
                    if (text.isNotEmpty() || words.isNotEmpty()) {
                        syncedLines.add(
                            SyncedLine(
                                line = text,
                                time = lineTimestamp.toInt(),
                                words = words.takeIf { it.isNotEmpty() }
                            )
                        )
                    }
                } else {
                    if (text.isNotEmpty()) {
                        syncedLines.add(
                            SyncedLine(
                                line = text,
                                time = lineTimestamp.toInt(),
                                words = null
                            )
                        )
                    }
                }
            } else {
                plainLines.add(line)
            }
        }

        return if (isSynced && syncedLines.isNotEmpty()) {
            Lyrics(plain = null, synced = syncedLines)
        } else if (plainLines.isNotEmpty()) {
            Lyrics(plain = plainLines, synced = null)
        } else {
            null
        }
    }

    private fun sanitizeLrcLine(raw: String): String {
        return raw.trim().replace("\r", "")
    }

    private fun stripLrcTimestamps(raw: String): String {
        return LRC_WORD_TAG_REGEX.replace(raw, "").trim()
    }
}
