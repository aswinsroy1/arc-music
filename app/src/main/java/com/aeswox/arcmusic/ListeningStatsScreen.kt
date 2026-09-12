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
            WeeklyActivitySection(
                weeklyMinutesByDay = stats.weeklyMinutesByDay,
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

@Composable
fun WeeklyActivitySection(
    weeklyMinutesByDay: List<Long>,
    modifier: Modifier = Modifier
) {
    // Day labels starting from 6 days ago → today
    val cal = java.util.Calendar.getInstance()
    val labels = (6 downTo 0).map { daysAgo ->
        val tmp = java.util.Calendar.getInstance()
        tmp.timeInMillis = cal.timeInMillis - daysAgo * 24L * 3600 * 1000
        when (tmp.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY    -> "M"
            java.util.Calendar.TUESDAY   -> "T"
            java.util.Calendar.WEDNESDAY -> "W"
            java.util.Calendar.THURSDAY  -> "T"
            java.util.Calendar.FRIDAY    -> "F"
            java.util.Calendar.SATURDAY  -> "S"
            else                         -> "S" // SUNDAY
        }
    }

    val maxMinutes = weeklyMinutesByDay.maxOrNull()?.takeIf { it > 0L } ?: 1L

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Weekly Activity",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Past 7 Days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        GlassCard(modifier = Modifier
            .fillMaxWidth()
            .height(256.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyMinutesByDay.forEachIndexed { index, minutes ->
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
                                    .fillMaxWidth(0.6f)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = opacity)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    val displayGenre = entry.genre.split(",", "/", ";").firstOrNull()?.trim() ?: entry.genre
                    
                    GenreCard(
                        icon = icon,
                        title = displayGenre,
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
 *  - The Night Owl        — peak period 10 PM–5 AM (hours 22..23, 0..4) (moonlit indigo → midnight blue)
 *  - The Early Bird       — peak period 5–11 AM (hours 5..10)            (warm amber → sunrise orange)
 *  - The Daytripper       — peak period 11 AM–5 PM (hours 11..16)         (vibrant yellow → fresh teal)
 *  - The Evening Unwinder — peak period 5–10 PM (hours 17..21)          (twilight rose → dusk violet)
 *  - The Free Spirit      — evenly distributed listening                (electric cyan → vivid purple)
 */
@Composable
fun NightOwlPersonalityCard(
    minutesByHour: List<Long>,
    modifier: Modifier = Modifier
) {
    // Divide 24 hours into 4 distinct, non-overlapping periods:
    // Morning (5 AM - 11 AM): hours 5..10
    // Daytime (11 AM - 5 PM): hours 11..16
    // Evening (5 PM - 10 PM): hours 17..21
    // Night (10 PM - 5 AM): hours 22..23, 0..4
    val morningHours = 5..10
    val dayHours = 11..16
    val eveningHours = 17..21
    val nightHours = listOf(22, 23, 0, 1, 2, 3, 4)

    val morningTotal = morningHours.sumOf { minutesByHour.getOrElse(it) { 0L } }
    val dayTotal = dayHours.sumOf { minutesByHour.getOrElse(it) { 0L } }
    val eveningTotal = eveningHours.sumOf { minutesByHour.getOrElse(it) { 0L } }
    val nightTotal = nightHours.sumOf { minutesByHour.getOrElse(it) { 0L } }

    val grandTotal = (morningTotal + dayTotal + eveningTotal + nightTotal).coerceAtLeast(1L)
    val maxTotal = maxOf(morningTotal, dayTotal, eveningTotal, nightTotal)
    val maxFraction = maxTotal.toFloat() / grandTotal.toFloat()

    // If the top block does not reach at least 35% of total listening, the pattern is too
    // spread out to call a time-based personality — show "The Free Spirit" instead.
    val isSpread = maxFraction < 0.35f

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
            gradientStart = Color(0xFF06B6D4),   // electric cyan
            gradientEnd   = Color(0xFFA855F7)    // vivid purple
        )
        maxTotal == morningTotal -> Personality(
            icon          = Icons.Default.LightMode,
            label         = "The Early Bird",
            blurb         = "Peak listening in the morning between 5 AM and 11 AM. You start every day with the right soundtrack.",
            gradientStart = Color(0xFFF59E0B),   // warm amber
            gradientEnd   = Color(0xFFF97316)    // sunrise orange
        )
        maxTotal == dayTotal -> Personality(
            icon          = Icons.Default.WbSunny,
            label         = "The Daytripper",
            blurb         = "Peak listening in the afternoon between 11 AM and 5 PM — music powers your day and keeps your rhythm flowing.",
            gradientStart = Color(0xFFEAB308),   // vibrant yellow
            gradientEnd   = Color(0xFF14B8A6)    // fresh teal
        )
        maxTotal == eveningTotal -> Personality(
            icon          = Icons.Default.Nightlight,
            label         = "The Evening Unwinder",
            blurb         = "Peak listening in the evening between 5 PM and 10 PM. The perfect way to wind down and close out the day.",
            gradientStart = Color(0xFFF43F5E),   // twilight rose
            gradientEnd   = Color(0xFF8B5CF6)    // dusk violet
        )
        else -> Personality(
            icon          = Icons.Default.DarkMode,
            label         = "The Night Owl",
            blurb         = "Peak listening late at night between 10 PM and 5 AM. You love the quiet hours and the music that fills them.",
            gradientStart = Color(0xFF818CF8),   // moonlit indigo
            gradientEnd   = Color(0xFF3B82F6)    // midnight blue
        )
    }

    val cardShape = RoundedCornerShape(AppCornerRadius)
    val tintGradient = Brush.linearGradient(
        colors = listOf(
            personality.gradientStart.copy(alpha = 0.16f),
            personality.gradientEnd.copy(alpha = 0.08f)
        )
    )
    val borderGradient = Brush.linearGradient(
        colors = listOf(
            personality.gradientStart.copy(alpha = 0.35f),
            personality.gradientEnd.copy(alpha = 0.15f)
        )
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = borderGradient,
                shape = cardShape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tintGradient)
                .padding(24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(personality.gradientStart.copy(alpha = 0.18f))
                        .border(
                            width = 1.dp,
                            color = personality.gradientStart.copy(alpha = 0.35f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = personality.icon,
                        contentDescription = null,
                        tint = personality.gradientStart,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = personality.label,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = personality.blurb,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
