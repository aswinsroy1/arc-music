package com.aeswox.arcmusic

import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.JellyIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledTonalIconButton
import com.aeswox.arcmusic.ui.components.JellyOutlinedIconButton

// Genre icon mapping — deterministic, not listening-based
private val genreIconMap = mapOf(
    "pop"        to Icons.Default.Headset,
    "indie"      to Icons.Default.Eco,
    "r&b"        to Icons.Default.Nightlight,
    "rnb"        to Icons.Default.Nightlight,
    "ambient"    to Icons.Default.Waves,
    "electronic" to Icons.Default.Waves,
    "rock"       to Icons.Default.MusicNote,
    "hip-hop"    to Icons.Default.MusicNote,
    "hiphop"     to Icons.Default.MusicNote,
    "jazz"       to Icons.Default.MusicNote,
    "classical"  to Icons.Default.MusicNote,
    "country"    to Icons.Default.MusicNote,
    "metal"      to Icons.Default.MusicNote,
    "folk"       to Icons.Default.Eco
)
private val genreContainerColors = listOf(
    { cs: ColorScheme -> cs.primaryContainer to cs.onPrimaryContainer },
    { cs: ColorScheme -> cs.secondaryContainer to cs.onSecondaryContainer },
    { cs: ColorScheme -> cs.tertiaryContainer to cs.onTertiaryContainer },
    { cs: ColorScheme -> cs.outlineVariant.copy(alpha = 0.3f) to cs.onSurface }
)

@Composable
fun ListeningStatsScreenContent(
    stats: ListeningStatsData,
    bottomPadding: Dp,
    onNavigateBack: () -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.physicsBounceOverscroll().fillMaxSize()
    ) {
        item {
            StatsHeader(onBackClick = onNavigateBack)
        }
        item {
            TotalListeningTimeCard(
                totalMinutes = stats.totalMinutes,
                weekOverWeekPct = stats.weekOverWeekPct,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        item {
            ActivitySection(
                dailyMinutes = stats.dailyMinutesByHour,
                weeklyMinutes = stats.weeklyMinutesByDay,
                monthlyMinutes = stats.monthlyMinutesByDay,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        if (stats.topArtists.isNotEmpty()) {
            item {
                TopArtistsSection(artists = stats.topArtists, onArtistClick = onNavigateToArtist)
            }
        }
        if (stats.topGenres.isNotEmpty()) {
            item {
                TopGenresSection(
                    genres = stats.topGenres,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
        // Night Owl card: always shown when there is any history
        if (stats.nightOwlMinutesByHour.any { it > 0L }) {
            item {
                NightOwlPersonalityCard(
                    minutesByHour = stats.nightOwlMinutesByHour,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
fun StatsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        JellyIconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "Listening Stats",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        // No profile photo — user-profile feature does not exist in this app
        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun TotalListeningTimeCard(
    totalMinutes: Long,
    weekOverWeekPct: Int?,
    modifier: Modifier = Modifier
) {
    val hours = totalMinutes / 60L
    val mins  = totalMinutes % 60L
    val displayText = when {
        hours > 0 -> if (mins > 0) "$hours hr $mins min" else "$hours Hours"
        else      -> "$mins min"
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "TOTAL LISTENING TIME",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 52.sp,
                    lineHeight = 62.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            // Trend line: only shown when we have prior-week data to compare
            if (weekOverWeekPct != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val (icon, tintColor) = if (weekOverWeekPct >= 0) {
                        Icons.AutoMirrored.Filled.TrendingUp to MaterialTheme.colorScheme.onSurface
                    } else {
                        Icons.AutoMirrored.Filled.TrendingDown to MaterialTheme.colorScheme.onSurface
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(18.dp)
                    )
                    val sign = if (weekOverWeekPct >= 0) "+" else ""
                    Text(
                        text = "${sign}${weekOverWeekPct}% from last week",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ActivitySection(
    dailyMinutes: List<Long>,
    weeklyMinutes: List<Long>,
    monthlyMinutes: List<Long>,
    modifier: Modifier = Modifier
) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 1,
        pageCount = { 3 }
    )
    val springSpec = androidx.compose.animation.core.spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 300f
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val title = when (pagerState.currentPage) {
                0 -> "Daily Activity"
                1 -> "Weekly Activity"
                2 -> "Monthly Activity"
                else -> "Activity"
            }
            val subtitle = when (pagerState.currentPage) {
                0 -> "Past 24 Hours"
                1 -> "Past 7 Days"
                2 -> "Past 30 Days"
                else -> ""
            }

            androidx.compose.animation.Crossfade(
                targetState = title,
                animationSpec = androidx.compose.animation.core.tween(300)
            ) { targetTitle ->
                Text(
                    text = targetTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            androidx.compose.animation.Crossfade(
                targetState = subtitle,
                animationSpec = androidx.compose.animation.core.tween(300)
            ) { targetSubtitle ->
                Text(
                    text = targetSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            flingBehavior = androidx.compose.foundation.pager.PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = springSpec
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
        ) { page ->
            GlassCard(modifier = Modifier.fillMaxSize()) {
                val data = when (page) {
                    0 -> dailyMinutes
                    1 -> weeklyMinutes
                    2 -> monthlyMinutes
                    else -> weeklyMinutes
                }

                val labels = when (page) {
                    0 -> {
                        val cal = java.util.Calendar.getInstance()
                        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                        (23 downTo 0).map { hoursAgo ->
                            val h = (currentHour - hoursAgo + 24) % 24
                            if (hoursAgo == 23 || hoursAgo == 11 || hoursAgo == 0) {
                                val amPm = if (h >= 12) "PM" else "AM"
                                val displayH = if (h % 12 == 0) 12 else h % 12
                                "$displayH$amPm"
                            } else ""
                        }
                    }
                    1 -> {
                        val cal = java.util.Calendar.getInstance()
                        (6 downTo 0).map { daysAgo ->
                            val tmp = java.util.Calendar.getInstance()
                            tmp.timeInMillis = cal.timeInMillis - daysAgo * 24L * 3600 * 1000
                            when (tmp.get(java.util.Calendar.DAY_OF_WEEK)) {
                                java.util.Calendar.MONDAY    -> "M"
                                java.util.Calendar.TUESDAY   -> "T"
                                java.util.Calendar.WEDNESDAY -> "W"
                                java.util.Calendar.THURSDAY  -> "T"
                                java.util.Calendar.FRIDAY    -> "F"
                                java.util.Calendar.SATURDAY  -> "S"
                                else                         -> "S"
                            }
                        }
                    }
                    2 -> {
                        (29 downTo 0).map { daysAgo ->
                            if (daysAgo == 29) "30d" else if (daysAgo == 14) "15d" else if (daysAgo == 0) "1d" else ""
                        }
                    }
                    else -> emptyList()
                }

                val maxMinutes = data.maxOrNull()?.takeIf { it > 0L } ?: 1L

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEachIndexed { index, minutes ->
                        val heightFraction = (minutes.toFloat() / maxMinutes).coerceIn(0.04f, 1f)
                        val opacity = (heightFraction * 0.85f + 0.15f).coerceIn(0.15f, 1f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            Box(
                                contentAlignment = Alignment.BottomCenter,
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (page == 1) 0.6f else 0.8f)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = opacity)
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = labels.getOrElse(index) { "" },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (page == 2) 8.sp else 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopArtistsSection(artists: List<ArtistStatEntry>, onArtistClick: (String) -> Unit) {
    Column {
        Text(
            text = "Top Artists",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),

            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(artists.size) { index ->
                val artist = artists[index]
                val hours = artist.totalMinutes / 60L
                val mins  = artist.totalMinutes % 60L
                val timeText = if (hours > 0) "$hours hr" else "$mins min"

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(112.dp)
                        .jellyClick { onArtistClick(artist.artistName) }
                ) {
                    com.aeswox.arcmusic.ui.components.ArtistImage(
                        model = artist.photoUri,
                        contentDescription = artist.artistName,
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = artist.artistName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "~$timeText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TopGenresSection(
    genres: List<GenreStatEntry>,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    // Pair genres into rows of 2
    val rows = genres.chunked(2)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Top Genres",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        rows.forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEachIndexed { colIndex, entry ->
                    val overallIndex = rowIndex * 2 + colIndex
                    val colorPair = genreContainerColors[overallIndex % genreContainerColors.size](cs)
                    val icon = genreIconMap[entry.genre.lowercase()] ?: Icons.Default.MusicNote
                    val timeLabel = when {
                        entry.totalMinutes <= 0L -> ""
                        entry.totalMinutes < 60L -> "${entry.totalMinutes} min"
                        else -> {
                            val h = entry.totalMinutes / 60L
                            val m = entry.totalMinutes % 60L
                            if (m > 0) "$h hr $m min" else "$h hr"
                        }
                    }
                    GenreCard(
                        icon = icon,
                        title = entry.genre,
                        tracks = timeLabel,
                        iconBg = colorPair.first,
                        iconTint = colorPair.second,
                        modifier = Modifier.weight(1f)
                    )
                }
                // If odd number of genres, fill remaining space
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun GenreCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tracks: String,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tracks,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Derives the listening personality from hour-of-day data.
 * Always shown once there is at least one non-zero hour bucket.
 *
 * Personalities:
 *  - The Night Owl          — peak window 0–5 AM   (midnight navy → deep indigo)
 *  - The Early Bird         — peak window 6–11 AM  (warm amber → sunrise orange)
 *  - The Afternoon Listener — peak window 12–17    (golden yellow → warm teal)
 *  - The Evening Unwinder   — peak window 18–23    (deep rose → violet dusk)
 *  - The Free Spirit        — no dominant window   (electric teal → vivid purple)
 */
@Composable
fun NightOwlPersonalityCard(
    minutesByHour: List<Long>,
    modifier: Modifier = Modifier
) {
    // Find the best 4-hour window
    val windowSize = 4
    var bestStart = 0
    var bestTotal = 0L
    for (start in 0 until 24) {
        val total = (0 until windowSize).sumOf { minutesByHour[(start + it) % 24] }
        if (total > bestTotal) { bestTotal = total; bestStart = start }
    }
    val peakEnd = (bestStart + windowSize) % 24
    val grandTotal = minutesByHour.sum().coerceAtLeast(1L)

    // If the top 4-hour window contains < 35% of total listening, the pattern is too
    // spread out to call a time-based personality — show "The Free Spirit" instead.
    val peakFraction = bestTotal.toFloat() / grandTotal.toFloat()
    val isSpread = peakFraction < 0.35f

    fun fmt(h: Int) = when {
        h == 0  -> "midnight"
        h < 12  -> "$h AM"
        h == 12 -> "noon"
        else    -> "${h - 12} PM"
    }

    data class Personality(
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val label: String,
        val blurb: String,
        val gradientStart: Color,
        val gradientEnd: Color
    )

    val personality = when {
        isSpread -> Personality(
            icon          = Icons.Default.Shuffle,
            label         = "The Free Spirit",
            blurb         = "Your listening has no rules — you play music whenever the mood strikes, day or night. Music just fits into every corner of your life.",
            gradientStart = Color(0xFF0EA5A0),   // electric teal
            gradientEnd   = Color(0xFF7C3AED)    // vivid purple
        )
        bestStart in 0..5 -> Personality(
            icon          = Icons.Default.DarkMode,
            label         = "The Night Owl",
            blurb         = "Most active between ${fmt(bestStart)} and ${fmt(peakEnd)}. You love the quiet hours and the music that fills them.",
            gradientStart = Color(0xFF0F172A),   // midnight navy
            gradientEnd   = Color(0xFF312E81)    // deep indigo
        )
        bestStart in 6..11 -> Personality(
            icon          = Icons.Default.LightMode,
            label         = "The Early Bird",
            blurb         = "Your listening peaks between ${fmt(bestStart)} and ${fmt(peakEnd)}. You start every day with the right soundtrack.",
            gradientStart = Color(0xFFB45309),   // warm amber
            gradientEnd   = Color(0xFFEA580C)    // sunrise orange
        )
        bestStart in 12..17 -> Personality(
            icon          = Icons.Default.WbSunny,
            label         = "The Afternoon Listener",
            blurb         = "Peak activity from ${fmt(bestStart)} to ${fmt(peakEnd)} — music is your productive afternoon fuel.",
            gradientStart = Color(0xFFD97706),   // golden yellow
            gradientEnd   = Color(0xFF0D9488)    // warm teal
        )
        else -> Personality(
            icon          = Icons.Default.Nightlight,
            label         = "The Evening Unwinder",
            blurb         = "You wind down with music between ${fmt(bestStart)} and ${fmt(peakEnd)}. The perfect way to close out the day.",
            gradientStart = Color(0xFF9F1239),   // deep rose
            gradientEnd   = Color(0xFF6D28D9)    // violet dusk
        )
    }

    val gradient = Brush.linearGradient(
        colors = listOf(personality.gradientStart, personality.gradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(32.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = personality.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = personality.label,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = personality.blurb,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 24.sp
                )
            }
        }
    }
}
