@file:Suppress("TooManyFunctions")
package com.aeswox.arcmusic

import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aeswox.arcmusic.data.model.SyncedLine
import com.aeswox.arcmusic.data.model.SyncedWord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.abs

// ──────────────────────────────────────────────────────────────────────────────
// Constants
// ──────────────────────────────────────────────────────────────────────────────

private const val UNSUNG_ALPHA = 0.45f
private const val GLOW_ALPHA = 0.62f
private val GLOW_RADIUS = 6.dp
private val GLOW_ROOM = 10.dp
private val WIPE_FEATHER = 30.dp
private val WORD_RISE = 2.dp
private val LINE_FALLOFF_ALPHA = floatArrayOf(1f, 0.8f, 0.7f, 0.58f, 0.46f)
private const val BROWSING_ALPHA = 0.8f
private const val INACTIVE_SCALE = 0.98f
private const val PRESSED_SCALE = 0.96f
private val LYRIC_EASING = CubicBezierEasing(0.41f, 0f, 0.12f, 0.99f)
private const val LYRIC_SETTLE_MS = 400
private const val STAGGER_STEPS = 3
private const val STAGGER_FRACTION = 0.06f
private const val SCROLL_LEAD_MIN_MS = 350L
private const val SCROLL_LEAD_MAX_MS = 500L
private const val GROW_HEADROOM = 3f
private const val RISE_MS = 700f

// GrowingWord animation constants
private const val GROW_MAX_CHARS = 7
private const val GROW_MIN_SOLO_MS = 1_100L
private const val GROW_MIN_SHORT_MS = 1_360L
private const val GROW_SHORT_STEP_MS = 140L
private const val GROW_MIN_FOUR_MS = 1_050L
private const val GROW_MIN_LONG_MS = 900L
private const val GROW_MS_PER_CHAR = 200L
private const val GROW_DECAY_LONG_CHARS = 5
private const val GROW_DECAY_QUICK_MS = 1_200f
private const val GROW_DECAY_QUICK_FLOOR_MS = 800f
private const val GROW_DECAY_LONG = 0.4f
private const val GROW_DECAY_QUICK = 0.3f
private const val GROW_DECAY_QUICK_TINY = 0.1f
private const val GROW_DECAY_MAX = 0.7f
private const val GROW_STAGGER = 0.09f
private const val GROW_SPAN = 1.5f
private const val GROW_IN = 0.25f
private const val GROW_HOLD = 0.30f
private const val GROW_OUT = 0.75f
private const val GROW_REST = 1f
private const val GROW_RAMP_MIN_MS = 400f
private const val GROW_RAMP_MAX_MS = 3_000f
private const val GROW_BASE_SHORT = 0.05f
private const val GROW_BASE_LONG = 0.04f
private const val GROW_SCALE_RANGE = 0.08f
private const val GROW_SCALE_CEILING = 0.1f
private const val GROW_SCALE_TRIM = 0.98f
private const val GROW_SHIFT_EM = 25f / 34f
private const val GROW_BLOOM_FLOOR = 0.35f
private const val GROW_BLOOM_RANGE = 0.45f
private const val GROW_BLOOM_PACE_MS = 1_500f
private const val GROW_BLOOM_PACE_MAX = 1.1f
private const val GROW_BLOOM_SHORT = 0.85f
private const val GROW_BLOOM_LONG = 1.1f
private const val GROW_LIFT_PACE_MS = 2_000f
private const val GROW_LIFT_FLOOR = 0.3f

// Auto-hide controls
internal const val LYRICS_CONTROLS_IDLE_MS = 5_000L

// ──────────────────────────────────────────────────────────────────────────────
// ScrollRun: tracks a panel scroll animation for row-stagger
// ──────────────────────────────────────────────────────────────────────────────

private data class ScrollRun(val id: Int, val delta: Float, val durationMs: Int) {
    val spanMs: Float get() = durationMs * (1f + STAGGER_FRACTION * STAGGER_STEPS)
}

// ──────────────────────────────────────────────────────────────────────────────
// Foreground guard — prevents frame-clock from running with screen off
// ──────────────────────────────────────────────────────────────────────────────

@Composable
internal fun rememberArcIsForeground(): Boolean {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var foreground by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { owner, _ ->
            foreground = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return foreground
}

// ──────────────────────────────────────────────────────────────────────────────
// Lyric clock — frame-accurate position, no drift
// ──────────────────────────────────────────────────────────────────────────────

/** Polling jitter must not rewind a word highlight or briefly reactivate the previous line. */
private fun reconcileArcLyricPosition(displayedMs: Long, reportedMs: Long): Long =
    if (abs(displayedMs - reportedMs) <= 250L) maxOf(displayedMs, reportedMs)
    else reportedMs

@Composable
internal fun rememberArcLyricClock(positionMs: Long, isPlaying: Boolean): MutableLongState {
    val clock = remember { mutableLongStateOf(positionMs) }
    val foreground = rememberArcIsForeground()
    LaunchedEffect(positionMs, isPlaying, foreground) {
        clock.longValue = reconcileArcLyricPosition(clock.longValue, positionMs)
        if (!isPlaying || !foreground) return@LaunchedEffect
        val firstFrame = withFrameMillis { it }
        while (true) {
            withFrameMillis { frame ->
                clock.longValue = maxOf(clock.longValue, positionMs + frame - firstFrame)
            }
        }
    }
    return clock
}

// ──────────────────────────────────────────────────────────────────────────────
// SyncedWord extensions: derive endMs from next word's start
// ──────────────────────────────────────────────────────────────────────────────

/** Derive end-ms for a word: next word's start, or start + 500ms fallback. */
private fun List<SyncedWord>.endMsAt(index: Int): Long {
    val next = getOrNull(index + 1)
    return if (next != null) next.time.toLong()
    else (get(index).time + 500).toLong()
}

// ──────────────────────────────────────────────────────────────────────────────
// SyncedLine physics extensions
// ──────────────────────────────────────────────────────────────────────────────

/** When this line's last word finishes. */
private fun SyncedLine.lineEndMs(): Long {
    val ws = words ?: return (time + 500).toLong()
    if (ws.isEmpty()) return (time + 500).toLong()
    return (ws.last().time + 500).toLong()
}

private fun smooth(fraction: Float) = fraction * fraction * (3f - 2f * fraction)

/** Whether anything on this line is off the floor at [positionMs]. */
private fun SyncedLine.isLifted(positionMs: Long): Boolean {
    val ws = words ?: return false
    if (ws.isEmpty()) return false
    if (positionMs <= ws.first().time.toLong()) return false
    return positionMs < ws.last().time.toLong() + RISE_MS.toLong() || isGrowing(positionMs)
}

/** 0..1 lift for the word at [index]. */
private fun SyncedLine.wordLift(index: Int, positionMs: Long): Float {
    val ws = words ?: return 0f
    val word = ws.getOrNull(index) ?: return 0f
    val endMs = ws.endMsAt(index)
    val rising = ((positionMs - word.time.toLong()) / RISE_MS).coerceIn(0f, 1f)
    val falling = (1f - (positionMs - endMs) / RISE_MS).coerceIn(0f, 1f)
    return smooth(minOf(rising, falling))
}

/** The settle fraction (1 while sung, easing to 0 over RISE_MS once past). */
private fun SyncedLine.wordFall(index: Int, positionMs: Long): Float {
    val ws = words ?: return 0f
    val endMs = ws.endMsAt(index)
    return smooth((1f - (positionMs - endMs) / RISE_MS).coerceIn(0f, 1f))
}

/** How many characters of this line have been revealed at [positionMs], fractionally. */
private fun SyncedLine.revealedChars(positionMs: Long): Float {
    val ws = words
    if (ws.isNullOrEmpty()) return if (positionMs >= time.toLong()) line.length.toFloat() else 0f
    var offset = 0
    ws.forEachIndexed { index, word ->
        val wordText = word.word
        val start = line.indexOf(wordText, offset).takeIf { it >= 0 } ?: offset
        val end = start + wordText.length
        if (positionMs < word.time.toLong()) return start.toFloat()
        val endMs = ws.endMsAt(index)
        if (positionMs < endMs) {
            val span = (endMs - word.time.toLong()).coerceAtLeast(1L)
            val through = (positionMs - word.time.toLong()).toFloat() / span
            return start + through * wordText.length
        }
        val next = ws.getOrNull(index + 1)
        if (next != null && positionMs < next.time.toLong()) {
            val gapStart = line.indexOf(next.word, end).takeIf { it >= 0 } ?: end
            val pause = (next.time.toLong() - endMs).coerceAtLeast(1L)
            val through = (positionMs - endMs).toFloat() / pause
            return end + through * (gapStart - end)
        }
        offset = end
    }
    return line.length.toFloat()
}

/** Character spans for each word within [line]. */
private val SyncedLine.wordSpans: List<IntRange>
    get() {
        val ws = words ?: return emptyList()
        var offset = 0
        return ws.map { word ->
            val start = line.indexOf(word.word, offset).takeIf { it >= 0 } ?: offset
            val end = start + word.word.length
            offset = end
            start until end
        }
    }

/** Words held long enough to animate a letter at a time. */
private val SyncedLine.growingWords: List<ArcGrowingWord>
    get() {
        val ws = words ?: return emptyList()
        return ws.mapIndexedNotNull { index, word ->
            val endMs = ws.endMsAt(index)
            if (arcCanGrow(word.word, word.time.toLong(), endMs)) ArcGrowingWord(index, word.word, word.time.toLong(), endMs) else null
        }
    }

/** The letter-by-letter treatment for word [index], where it has earned one. */
private fun SyncedLine.growingAt(index: Int): ArcGrowingWord? =
    growingWords.firstOrNull { it.index == index }

/** Whether any word on this line is mid-flight at [positionMs]. */
private fun SyncedLine.isGrowing(positionMs: Long): Boolean =
    growingWords.any { positionMs >= it.startMs && positionMs <= it.restsAtMs }

// ──────────────────────────────────────────────────────────────────────────────
// GrowingWord — per-character swell physics
// ──────────────────────────────────────────────────────────────────────────────

private fun arcCanGrow(text: String, startMs: Long, endMs: Long): Boolean {
    val length = text.length
    if (length == 0 || length > GROW_MAX_CHARS) return false
    if ('-' in text || text.any { it.isBlockScript() || it.isJoinedScript() }) return false
    val held = endMs - startMs
    return when {
        length == 1 -> held >= GROW_MIN_SOLO_MS
        length <= 3 -> held >= GROW_MIN_SHORT_MS + (length - 2) * GROW_SHORT_STEP_MS
        length == 4 -> held >= GROW_MIN_FOUR_MS
        else -> held >= GROW_MIN_LONG_MS && held >= length * GROW_MS_PER_CHAR
    }
}

private fun Char.isBlockScript(): Boolean =
    this in '一'..'鿿' || this in '぀'..'ゟ' || this in '゠'..'ヿ' || this in '가'..'힯'

private fun Char.isJoinedScript(): Boolean = this in '֐'..'ࣿ'

private fun arcDecayRate(length: Int, heldMs: Float): Float {
    val long = length > GROW_DECAY_LONG_CHARS
    val quick = heldMs < GROW_DECAY_QUICK_MS
    if (!long && !quick) return 0f
    var strength = 0f
    if (long) strength += minOf((length - GROW_DECAY_LONG_CHARS) / 5f, 1f) * GROW_DECAY_LONG
    if (quick) {
        val short = maxOf(0f, 1f - (heldMs - GROW_DECAY_QUICK_FLOOR_MS) / 400f)
        strength += short * if (length > 3) GROW_DECAY_QUICK else GROW_DECAY_QUICK_TINY
    }
    return minOf(strength, GROW_DECAY_MAX)
}

/** One word held long enough to animate letter-by-letter. */
internal class ArcGrowingWord(
    val index: Int,
    text: String,
    val startMs: Long,
    val endMs: Long,
) {
    private val chars: Int = text.length
    private val scalePeak = FloatArray(chars)
    private val shiftPeak = FloatArray(chars)
    private val risePeak = FloatArray(chars)
    private val bloomPeak = FloatArray(chars)
    val restsAtMs: Long

    init {
        val held = (endMs - startMs).coerceAtLeast(1L).toFloat()
        val earned = ((held - GROW_RAMP_MIN_MS) / (GROW_RAMP_MAX_MS - GROW_RAMP_MIN_MS))
            .coerceIn(0f, 1f).let { it * it * it }
        val decay = arcDecayRate(chars, held)
        val bloomPace = minOf(GROW_BLOOM_PACE_MAX, held / GROW_BLOOM_PACE_MS)
        val bloomSpread = when { chars <= 3 -> GROW_BLOOM_SHORT; chars >= 6 -> GROW_BLOOM_LONG; else -> 1f }
        val base = if (chars <= 3) GROW_BASE_SHORT else GROW_BASE_LONG
        val liftPace = (held / GROW_LIFT_PACE_MS).coerceIn(GROW_LIFT_FLOOR, 1f)
        for (i in 0 until chars) {
            val place = if (chars > 1) i.toFloat() / (chars - 1) else 0f
            val reach = earned * (1f - place * decay)
            val scale = 1f + base + reach * GROW_SCALE_RANGE
            scalePeak[i] = scale * GROW_SCALE_TRIM
            bloomPeak[i] = (GROW_BLOOM_FLOOR + reach * GROW_BLOOM_RANGE) * bloomPace * bloomSpread
            risePeak[i] = ((scale - 1f) / GROW_SCALE_CEILING) * liftPace
            val centre = (i + 0.5f) / chars
            shiftPeak[i] = (centre - 0.5f) * 2f * (scale - 1f) * GROW_SHIFT_EM * GROW_SCALE_TRIM
        }
        val last = (chars - 1).coerceAtLeast(0) * GROW_STAGGER + GROW_SPAN
        restsAtMs = startMs + (held * last).toLong()
    }

    fun sampleInto(charIndex: Int, positionMs: Long, into: ArcCharGrowth) {
        val span = (endMs - startMs).coerceAtLeast(1L).toFloat()
        val elapsed = positionMs - startMs - charIndex * span * GROW_STAGGER
        val phase = (elapsed / (span * GROW_SPAN)).coerceIn(0f, 1f)
        val peakScale = scalePeak[charIndex]
        when {
            phase < GROW_IN -> {
                val t = smooth(phase / GROW_IN)
                into.scale = 1f + (peakScale - 1f) * t
                into.shift = shiftPeak[charIndex] * t
                into.rise = risePeak[charIndex] * t
                into.bloom = bloomPeak[charIndex] * t
            }
            phase < GROW_HOLD -> {
                into.scale = peakScale
                into.shift = shiftPeak[charIndex]
                into.rise = risePeak[charIndex]
                into.bloom = bloomPeak[charIndex]
            }
            phase < GROW_OUT -> {
                val t = smooth((phase - GROW_HOLD) / (GROW_OUT - GROW_HOLD))
                into.scale = peakScale + (1f - peakScale) * t
                into.shift = shiftPeak[charIndex] * (1f - t)
                into.rise = risePeak[charIndex] + (GROW_REST - risePeak[charIndex]) * t
                into.bloom = bloomPeak[charIndex] * (1f - t)
            }
            else -> {
                into.scale = 1f; into.shift = 0f; into.rise = GROW_REST; into.bloom = 0f
            }
        }
    }
}

/** Scratch object — filled in per letter, per frame, per layer. */
internal class ArcCharGrowth {
    var scale: Float = 1f
    var shift: Float = 0f
    var rise: Float = 0f
    var bloom: Float = 0f
}

// ──────────────────────────────────────────────────────────────────────────────
// Active rows and scroll lead
// ──────────────────────────────────────────────────────────────────────────────

internal fun arcActiveLyricRows(lines: List<SyncedLine>, positionMs: Long): List<Int> {
    val latest = lines.indexOfLast { it.time.toLong() <= positionMs }
    if (latest < 0) return emptyList()
    return (0..latest).filter { index ->
        val line = lines[index]
        index == latest || (line.words != null && line.words.isNotEmpty() &&
            line.time.toLong() <= positionMs && positionMs < line.lineEndMs())
    }
}

internal fun arcScrollLead(lines: List<SyncedLine>, positionMs: Long): Long {
    val current = lines.indexOfLast { it.time.toLong() <= positionMs }
    if (current < 0) return SCROLL_LEAD_MIN_MS
    val next = lines.getOrNull(current + 1) ?: return SCROLL_LEAD_MIN_MS
    val gap = next.time.toLong() - lines[current].lineEndMs()
    return gap.coerceIn(SCROLL_LEAD_MIN_MS, SCROLL_LEAD_MAX_MS)
}

// ──────────────────────────────────────────────────────────────────────────────
// Draw helpers
// ──────────────────────────────────────────────────────────────────────────────

/** Where offset [offset] sits on visual line [visualLine], clamped to line edges. */
private fun TextLayoutResult.xOn(offset: Int, visualLine: Int, inset: Float): Float {
    val left = getLineLeft(visualLine) + inset
    val right = getLineRight(visualLine) + inset
    return when {
        offset <= getLineStart(visualLine) -> left
        offset >= getLineEnd(visualLine, visibleEnd = true) -> right
        else -> (getHorizontalPosition(offset, usePrimaryDirection = true) + inset).coerceIn(left, right)
    }
}

/** Fractional character horizontal position across visual line [visualLine]. */
private fun horizontalAt(layout: TextLayoutResult, chars: Float, visualLine: Int): Float {
    val lineStart = layout.getLineStart(visualLine)
    val lineEnd = layout.getLineEnd(visualLine, visibleEnd = true)
    val index = chars.toInt().coerceIn(lineStart, lineEnd)
    val here = layout.xOn(index, visualLine, 0f)
    val next = layout.xOn((index + 1).coerceAtMost(lineEnd), visualLine, 0f)
    return here + (next - here) * (chars - index)
}

/** One flat slice of a risen line. */
private fun ContentDrawScope.sliceRisen(from: Float, top: Float, to: Float, bottom: Float, dy: Float) {
    if (to <= from) return
    clipRect(left = from, top = top, right = to, bottom = bottom) {
        translate(top = dy) { this@sliceRisen.drawContent() }
    }
}

/**
 * Clips the lit layer to the first [revealedChars] characters, with an optional feathered edge.
 */
private fun ContentDrawScope.sweepTo(layout: TextLayoutResult, revealedChars: Float, feather: Boolean) {
    if (revealedChars <= 0f) return
    if (revealedChars >= layout.layoutInput.text.length) { drawContent(); return }
    for (visualLine in 0 until layout.lineCount) {
        val start = layout.getLineStart(visualLine)
        if (revealedChars <= start) return
        val end = layout.getLineEnd(visualLine, visibleEnd = true)
        val cut = revealedChars < end
        val overhang = GLOW_ROOM.toPx()
        val internalOverhang = 12f
        val verticalShift = 26f
        val right = if (cut) horizontalAt(layout, revealedChars, visualLine) + internalOverhang else layout.getLineRight(visualLine) + overhang
        val top = if (visualLine == 0) layout.getLineTop(visualLine) - overhang else layout.getLineTop(visualLine) + verticalShift
        val bottom = if (visualLine == layout.lineCount - 1) layout.getLineBottom(visualLine) + overhang else layout.getLineBottom(visualLine) + verticalShift
        val left = layout.getLineLeft(visualLine) - overhang

        clipRect(left = left, top = top, right = right, bottom = bottom) {
            this@sweepTo.drawContent()
        }
        if (!feather || !cut) continue
        clipRect(top = top, bottom = bottom) {
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.White, 1f to Color.Transparent,
                    startX = (right - WIPE_FEATHER.toPx()).coerceAtLeast(layout.getLineLeft(visualLine)),
                    endX = right,
                ),
                blendMode = BlendMode.DstIn,
            )
        }
    }
}

/** Lifts words off the baseline using clip-and-replay per word. */
private fun ContentDrawScope.riseWith(
    layout: TextLayoutResult, line: SyncedLine, positionMs: Long,
    inset: Float, peak: Float, growth: ArcCharGrowth,
) {
    if (!line.isLifted(positionMs)) { drawContent(); return }
    val em = layout.layoutInput.style.fontSize.toPx()
    val overhang = GLOW_ROOM.toPx()
    val spans = line.wordSpans
    for (visualLine in 0 until layout.lineCount) {
        val lineStart = layout.getLineStart(visualLine)
        val lineEnd = layout.getLineEnd(visualLine, visibleEnd = true)
        val verticalShift = 26f
        val top = if (visualLine == 0) layout.getLineTop(visualLine) + inset - overhang else layout.getLineTop(visualLine) + inset + verticalShift
        val bottom = if (visualLine == layout.lineCount - 1) layout.getLineBottom(visualLine) + inset + overhang else layout.getLineBottom(visualLine) + inset + verticalShift
        var at = lineStart
        var edge = layout.getLineLeft(visualLine) + inset - overhang
        val ws = line.words ?: continue
        for (index in ws.indices) {
            val span = spans.getOrNull(index) ?: continue
            val start = maxOf(span.first, lineStart)
            val end = minOf(span.last + 1, lineEnd)
            if (start >= end) continue
            val held = line.growingAt(index)?.takeIf { positionMs in it.startMs..it.restsAtMs }
            val lift = line.wordLift(index, positionMs)
            if (held == null && lift <= 0.01f) continue
            val from = layout.xOn(start, visualLine, inset)
            val to = layout.xOn(end, visualLine, inset)
            if (to <= from) continue
            
            val internalOverhang = 12f
            val wordFrom = if (start == lineStart) from - overhang else from - internalOverhang
            val wordTo = if (end == lineEnd) to + overhang else to + internalOverhang

            if (start > at) sliceRisen(edge, top, wordFrom, bottom, 0f)
            
            if (held != null) {
                growEach(layout, held, line, positionMs, visualLine, start, end, top, bottom, inset, peak, em, growth)
            } else {
                sliceRisen(wordFrom, top - peak, wordTo, bottom, -lift * peak)
            }
            at = end; edge = wordTo
        }
        if (at < lineEnd) sliceRisen(edge, top, layout.getLineRight(visualLine) + inset + overhang, bottom, 0f)
    }
}

/** Letter-by-letter swell for a held word. */
private fun ContentDrawScope.growEach(
    layout: TextLayoutResult, word: ArcGrowingWord, line: SyncedLine,
    positionMs: Long, visualLine: Int, start: Int, end: Int,
    top: Float, bottom: Float, inset: Float, peak: Float, em: Float, growth: ArcCharGrowth,
) {
    val fall = line.wordFall(word.index, positionMs)
    val first = line.wordSpans.getOrNull(word.index)?.first ?: return
    val ceiling = top - peak * GROW_HEADROOM
    val middle = (top + bottom) / 2f
    for (char in start until end) {
        word.sampleInto(char - first, positionMs, growth)
        val from = layout.xOn(char, visualLine, inset)
        val to = layout.xOn(char + 1, visualLine, inset)
        if (to <= from) continue
        val dx = growth.shift * em
        val dy = -growth.rise * peak * fall
        val overhang = (to - from) * (growth.scale - 1f) / 2f
        clipRect(left = from - overhang + dx, top = ceiling, right = to + overhang + dx, bottom = bottom) {
            translate(left = dx, top = dy) {
                scale(growth.scale, growth.scale, Offset((from + to) / 2f, middle)) {
                    this@growEach.drawContent()
                }
            }
        }
    }
}

/** Bloom mask: draws each held-note letter clipped to its own brightness. */
private fun ContentDrawScope.glowGrown(
    layout: TextLayoutResult, line: SyncedLine, positionMs: Long,
    inset: Float, peak: Float, growth: ArcCharGrowth,
) {
    if (!line.isGrowing(positionMs)) return
    val em = layout.layoutInput.style.fontSize.toPx()
    val length = layout.layoutInput.text.length
    for (word in line.growingWords) {
        if (positionMs < word.startMs || positionMs > word.restsAtMs) continue
        val span = line.wordSpans.getOrNull(word.index) ?: continue
        val fall = line.wordFall(word.index, positionMs)
        for (char in span.first..minOf(span.last, length - 1)) {
            word.sampleInto(char - span.first, positionMs, growth)
            if (growth.bloom <= 0.01f) continue
            val visualLine = layout.getLineForOffset(char)
            val from = layout.xOn(char, visualLine, inset)
            val to = layout.xOn(char + 1, visualLine, inset)
            if (to <= from) continue
            val dx = growth.shift * em
            val dy = -growth.rise * peak * fall
            val verticalShift = 26f
            val rowTop = if (visualLine == 0) layout.getLineTop(visualLine) + inset - GLOW_ROOM.toPx() else layout.getLineTop(visualLine) + inset + verticalShift
            val bottom = if (visualLine == layout.lineCount - 1) layout.getLineBottom(visualLine) + inset + GLOW_ROOM.toPx() else layout.getLineBottom(visualLine) + inset + verticalShift
            val overhang = (to - from) * (growth.scale - 1f) / 2f
            clipRect(
                left = from - overhang + dx, top = rowTop - peak * GROW_HEADROOM,
                right = to + overhang + dx + GLOW_ROOM.toPx(), bottom = bottom,
            ) {
                translate(left = dx, top = dy) {
                    scale(growth.scale, growth.scale, Offset((from + to) / 2f, (rowTop + bottom) / 2f)) {
                        this@glowGrown.drawContent()
                    }
                }
                drawRect(color = Color.White.copy(alpha = growth.bloom), blendMode = BlendMode.DstIn)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// ArcSweptLyricLine — the 3-layer composable
// ──────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ArcSweptLyricLine(
    line: SyncedLine,
    clock: MutableLongState,
    style: TextStyle,
    dimAlpha: Float,
    textColor: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    glowAlpha: Float = 0f,
    feather: Boolean = false,
    rise: Boolean = true,
    alignEnd: Boolean = false,
) {
    var layout by remember(line) { mutableStateOf<TextLayoutResult?>(null) }
    val growth = remember { ArcCharGrowth() }
    val room = Modifier.padding(GLOW_ROOM)

    val riseAgainst: (Modifier) -> Modifier = { inner ->
        if (!rise || line.words.isNullOrEmpty()) {
            inner
        } else {
            Modifier.drawWithContent {
                val measured = layout
                if (measured == null) drawContent()
                else riseWith(measured, line, clock.longValue, if (glowAlpha > 0f) GLOW_ROOM.toPx() else 0f, WORD_RISE.toPx(), growth)
            }.then(inner)
        }
    }

    val sweep = Modifier.drawWithContent {
        val position = clock.longValue
        val endMs = line.lineEndMs()
        when {
            position >= endMs -> drawContent()
            position <= line.time.toLong() -> Unit
            else -> layout?.let { sweepTo(it, line.revealedChars(position), feather) }
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier,
        contentAlignment = if (alignEnd) Alignment.TopEnd else Alignment.TopStart,
    ) {
        // Layer 1: dim base
        Text(
            text = line.line,
            style = style,
            color = textColor.copy(alpha = dimAlpha),
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = maxLines,
            overflow = overflow,
            onTextLayout = { layout = it },
            modifier = riseAgainst(room),
        )
        // Layer 2: bloom glow (API 31+ only)
        if (glowAlpha > 0.01f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Text(
                text = line.line,
                style = style,
                color = textColor,
                maxLines = maxLines,
                overflow = overflow,
                modifier = Modifier
                    .graphicsLayer { alpha = glowAlpha }
                    .blur(GLOW_RADIUS, BlurredEdgeTreatment.Unbounded)
                    .then(room)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        val measured = layout ?: return@drawWithContent
                        glowGrown(measured, line, clock.longValue, GLOW_ROOM.toPx(), WORD_RISE.toPx(), growth)
                    },
            )
        }
        // Layer 3: lit sweep
        Text(
            text = line.line,
            style = style,
            color = textColor,
            maxLines = maxLines,
            overflow = overflow,
            modifier = riseAgainst(
                Modifier
                    .graphicsLayer {
                        compositingStrategy = if (feather) CompositingStrategy.Offscreen
                        else CompositingStrategy.Auto
                    }
                    .then(room)
                    .then(sweep)
            ),
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Fading edges modifier
// ──────────────────────────────────────────────────────────────────────────────

internal fun Modifier.arcFadingEdges(fade: androidx.compose.ui.unit.Dp = 28.dp): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadePx = fade.toPx()
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black), startY = 0f, endY = fadePx),
            blendMode = BlendMode.DstIn,
        )
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = size.height - fadePx, endY = size.height),
            blendMode = BlendMode.DstIn,
        )
    }

// ──────────────────────────────────────────────────────────────────────────────
// NestedScrollConnection — traps scroll gestures inside the list
// ──────────────────────────────────────────────────────────────────────────────

private fun arcKeepScrollInList(listState: LazyListState) = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available
    override suspend fun onPreFling(available: Velocity): Velocity =
        if (available.y > 0f && !listState.canScrollBackward) available else Velocity.Zero
    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}

// ──────────────────────────────────────────────────────────────────────────────
// ArcLyricsPanel — the full LazyColumn panel
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Apple Music-style lyrics panel:
 *  - frame-accurate clock via [rememberArcLyricClock]
 *  - sweep + glow per line
 *  - word lift and letter swell for held notes
 *  - scroll leads playhead by 350–500ms
 *  - rows fan out with stagger on line change
 *  - scrolling up/down reveals/hides the player controls
 *  - tapping a line seeks to it
 */
@Composable
internal fun ArcLyricsPanel(
    lines: List<SyncedLine>,
    positionMs: Long,
    isPlaying: Boolean,
    textColor: Color,
    onSeekToLine: (Long) -> Unit,
    controlsOpen: Boolean,
    onRevealControls: () -> Unit,
    onHideControls: () -> Unit,
    onScrollingChange: (Boolean) -> Unit = {},
    isHeroMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val clock = rememberArcLyricClock(positionMs, isPlaying)
    val isSynced = remember(lines) { lines.any { it.time > 0 } }

    val activeRows by remember(lines, isSynced) {
        derivedStateOf {
            if (!isSynced) emptyList() else arcActiveLyricRows(lines, clock.longValue)
        }
    }
    val scrollLine = activeRows.firstOrNull() ?: -1
    val leadLine by remember(lines, isSynced) {
        derivedStateOf {
            if (!isSynced) -1
            else {
                val now = clock.longValue
                arcActiveLyricRows(lines, now + arcScrollLead(lines, now)).firstOrNull() ?: -1
            }
        }
    }
    val focusLine = if (leadLine >= 0) leadLine else scrollLine
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.collect(onScrollingChange)
    }

    val viewportHeight by remember(listState) {
        derivedStateOf { listState.layoutInfo.viewportSize.height }
    }
    val keepScroll = remember(listState) { arcKeepScrollInList(listState) }
    var browsing by remember { mutableStateOf(false) }

    // Controls scroll visibility
    val controlsSlopPx = with(androidx.compose.ui.platform.LocalDensity.current) { 20.dp.toPx() }
    val controlsOnScroll = remember(listState, controlsSlopPx) {
        object : NestedScrollConnection {
            private var travel = 0f
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    if (travel != 0f && (travel > 0f) != (available.y > 0f)) travel = 0f
                    travel += available.y
                    if (travel <= -controlsSlopPx) { travel = 0f; onHideControls() }
                    else if (travel >= controlsSlopPx) { travel = 0f; onRevealControls() }
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) browsing = true
        }
    }

    val currentLine by rememberUpdatedState(focusLine)
    val activeOnScreen by remember(listState) {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.any { it.index == currentLine } }
    }
    LaunchedEffect(browsing, activeOnScreen, listState.isScrollInProgress) {
        if (browsing && activeOnScreen && !listState.isScrollInProgress) {
            delay(600)
            browsing = false
        }
    }
    LaunchedEffect(browsing, listState.isScrollInProgress) {
        if (browsing && !listState.isScrollInProgress) {
            delay(5_000)
            browsing = false
        }
    }

    // Stagger animation
    var run by remember(lines) { mutableStateOf(ScrollRun(0, 0f, LYRIC_SETTLE_MS)) }
    val since = remember(lines) { mutableFloatStateOf(0f) }
    LaunchedEffect(run.id) {
        if (run.id == 0) return@LaunchedEffect
        animate(0f, run.spanMs, animationSpec = tween(run.spanMs.toInt(), easing = androidx.compose.animation.core.LinearEasing)) { value, _ ->
            since.floatValue = value
        }
    }

    var placed by remember(lines) { mutableStateOf(false) }

    LaunchedEffect(focusLine, browsing, controlsOpen) {
        if (isSynced && !browsing && focusLine >= 0 && focusLine in lines.indices) {
            snapshotFlow { listState.layoutInfo.viewportSize.height }.first { it > 0 }
            val visible = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == focusLine }
            when {
                !placed -> { listState.scrollToItem(focusLine, scrollOffset = 0); placed = true }
                visible != null -> {
                    val span = arcScrollLead(lines, clock.longValue).toInt()
                    run = ScrollRun(run.id + 1, visible.offset.toFloat(), span)
                    listState.animateScrollBy(visible.offset.toFloat(), animationSpec = tween(durationMillis = span, easing = LYRIC_EASING))
                }
                else -> listState.animateScrollToItem(focusLine, scrollOffset = 0)
            }
        }
    }

    if (lines.isEmpty()) {
        androidx.compose.foundation.layout.Box(modifier, contentAlignment = Alignment.Center) {
            Text("No lyrics available", style = MaterialTheme.typography.titleMedium, color = textColor.copy(alpha = 0.6f))
        }
        return
    }

    LazyColumn(
        state = listState,
        userScrollEnabled = !isHeroMode,
        modifier = modifier
            .nestedScroll(controlsOnScroll)
            .nestedScroll(keepScroll)
            .arcFadingEdges(if (isHeroMode) 12.dp else 28.dp),
        contentPadding = if (isHeroMode) PaddingValues(
            top = 40.dp,
            bottom = 40.dp,
            start = 8.dp,
            end = 8.dp,
        ) else PaddingValues(
            top = 40.dp - GLOW_ROOM,
            bottom = with(androidx.compose.ui.platform.LocalDensity.current) { viewportHeight.toDp() } * 0.8f,
            start = 28.dp - GLOW_ROOM,
            end = 28.dp - GLOW_ROOM,
        ),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(0.dp),
    ) {
        itemsIndexed(lines) { index, line ->
            val offset = if (scrollLine < 0) 0 else index - scrollLine
            val distance = abs(offset)
            val isActive = isSynced && index in activeRows
            val step = distance.coerceAtMost(LINE_FALLOFF_ALPHA.lastIndex)
            val lineAlpha by animateFloatAsState(
                targetValue = when {
                    !isSynced -> 0.95f
                    isActive -> 1f
                    browsing -> BROWSING_ALPHA
                    else -> LINE_FALLOFF_ALPHA[step]
                },
                animationSpec = tween(LYRIC_SETTLE_MS, easing = LYRIC_EASING),
                label = "lyricAlpha",
            )

            val sung = offset < 0
            val behind = if (run.delta >= 0f) index - focusLine else focusLine - index
            val staggerDelay = behind.coerceIn(0, STAGGER_STEPS) * STAGGER_FRACTION * run.durationMs
            val interaction = remember { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = when { pressed -> PRESSED_SCALE; isActive -> 1f; else -> INACTIVE_SCALE },
                animationSpec = tween(durationMillis = if (pressed) 120 else LYRIC_SETTLE_MS, easing = LYRIC_EASING),
                label = "lyricScale",
            )

            val glowEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val glow by animateFloatAsState(
                targetValue = if (isActive && glowEnabled) GLOW_ALPHA else 0f,
                animationSpec = tween(durationMillis = 420),
                label = "lyricGlow",
            )

            val style = if (isSynced) {
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isHeroMode) 24.sp else 34.sp,
                    lineHeight = if (isHeroMode) 34.sp else 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            } else {
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isHeroMode) 22.sp else 30.sp,
                    lineHeight = if (isHeroMode) 30.sp else 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            val alignEnd = line.voice % 2 == 0

            val shape = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    transformOrigin = TransformOrigin(if (alignEnd) 1f else 0f, 0.5f)
                    alpha = lineAlpha
                    translationY = if (staggerDelay <= 0f) 0f else {
                        val elapsed = since.floatValue
                        run.delta * (
                            LYRIC_EASING.transform((elapsed / run.durationMs).coerceIn(0f, 1f)) -
                            LYRIC_EASING.transform(((elapsed - staggerDelay) / run.durationMs).coerceIn(0f, 1f))
                        )
                    }
                }
                .clip(RoundedCornerShape(10.dp))
                .clickable(
                    enabled = isSynced,
                    interactionSource = interaction,
                    indication = androidx.compose.foundation.LocalIndication.current,
                ) { onSeekToLine(line.time.toLong()) }

            // Word-synced path
            if (!line.words.isNullOrEmpty() && isSynced) {
                val tail by animateFloatAsState(
                    targetValue = if (sung) 1f else UNSUNG_ALPHA,
                    label = "lyricTail",
                )
                ArcSweptLyricLine(
                    line = line,
                    clock = clock,
                    style = style,
                    dimAlpha = tail,
                    textColor = textColor,
                    glowAlpha = glow,
                    feather = isActive,
                    alignEnd = alignEnd,
                    modifier = shape,
                )
            } else {
                // Line-synced or plain: whole-line highlight
                val lit by animateFloatAsState(
                    targetValue = if (!isSynced || sung || isActive) 1f else UNSUNG_ALPHA,
                    label = "lyricLit",
                )
                Text(
                    text = line.line,
                    style = style,
                    color = textColor.copy(alpha = lit),
                    textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                    modifier = shape.padding(GLOW_ROOM),
                )
            }
        }
    }
}
