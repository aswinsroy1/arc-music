@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
package com.aeswox.arcmusic

import androidx.compose.animation.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.isActive

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage

import coil.request.ImageRequest

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration


import android.graphics.drawable.BitmapDrawable

import androidx.compose.foundation.Canvas

import androidx.compose.foundation.gestures.detectVerticalDragGestures

import androidx.compose.foundation.gestures.detectHorizontalDragGestures

import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Path

import androidx.compose.ui.graphics.StrokeCap

import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.layout.onSizeChanged

import kotlin.math.abs

import kotlin.math.sin

import dev.chrisbanes.haze.HazeState

import dev.chrisbanes.haze.haze

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.border

import com.aeswox.arcmusic.db.entities.Track

import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.animations.LocalJigglePhysicsSettings
import com.aeswox.arcmusic.ui.components.*
import com.aeswox.arcmusic.data.model.LyricsDisplayStyle
import com.aeswox.arcmusic.data.model.SyncedLine
import androidx.compose.ui.graphics.luminance

import kotlinx.coroutines.delay



fun formatDuration(durationMs: Long): String {

    val totalSeconds = durationMs / 1000

    val minutes = totalSeconds / 60

    val seconds = totalSeconds % 60

    if (totalSeconds < 0) return "0:00"

    return String.format("%d:%02d", minutes, seconds)

}



@Composable

fun HiResLogo(modifier: Modifier = Modifier, color: Color) {

    Box(

        modifier = modifier

            .border(1.dp, color, RoundedCornerShape(2.dp))

            .padding(horizontal = 5.dp, vertical = 3.dp),

        contentAlignment = Alignment.Center

    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(

                text = "Hi-Res",

                style = MaterialTheme.typography.labelSmall.copy(

                    fontSize = 9.sp,

                    fontWeight = FontWeight.Black,

                    letterSpacing = 0.sp

                ),

                color = color

            )

            Text(

                text = "AUDIO",

                style = MaterialTheme.typography.labelSmall.copy(

                    fontSize = 7.sp,

                    fontWeight = FontWeight.Bold,

                    letterSpacing = 1.sp

                ),

                color = color

            )

        }
    }
}

@Composable
fun LosslessLogo(modifier: Modifier = Modifier, color: Color) {
    Row(
        modifier = modifier

            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))

            .padding(horizontal = 6.dp, vertical = 4.dp),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(4.dp)

    ) {

        Icon(

            imageVector = Icons.Rounded.GraphicEq,

            contentDescription = "Lossless",

            tint = color,

            modifier = Modifier.size(12.dp)

        )

        Text(

            text = "Lossless",

            style = MaterialTheme.typography.labelSmall.copy(

                fontSize = 10.sp,

                fontWeight = FontWeight.Bold

            ),

            color = color

        )

    }

}



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun ArcNowPlayingScreen(

    tintTransparency: Float,

    noiseFactor: Float,

    glowIntensity: Float,

    isDarkTheme: Boolean,

    onNavigateBack: () -> Unit,

    onNavigateToQueue: () -> Unit = {},

    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    onNavigateToShare: (String, String) -> Unit = { _, _ -> },
    onNavigateToEditMetadata: (String) -> Unit = {}
) {

    val context = LocalContext.current

    val viewModel: MusicViewModel = hiltViewModel()

    val currentlyPlayingEntity by viewModel.currentlyPlaying.collectAsState()

    val randomPicks by viewModel.randomPicks.collectAsState()

    val libraryTracks by viewModel.libraryTracks.collectAsState()

    

    val rawSongToPlay = currentlyPlayingEntity ?: randomPicks.firstOrNull()

    val songToPlay = libraryTracks.find { it.id == rawSongToPlay?.id } ?: rawSongToPlay

    val hazeState = remember { HazeState() }


    var showOptionsSheet by remember { mutableStateOf(false) }

    var showAddToPlaylistSheet by remember { mutableStateOf(false) }

    var showDetailsDialog by remember { mutableStateOf(false) }

    var showSleepTimerDialog by remember { mutableStateOf(false) }

    var showDeviceSheet by remember { mutableStateOf(false) }

    var showLyrics by remember { mutableStateOf(false) }

    

    val deviceVolume by viewModel.deviceVolume.collectAsState()

    val deviceMaxVolume by viewModel.deviceMaxVolume.collectAsState()

    val shuffleEnabled by viewModel.shuffleModeEnabled.collectAsState()

    val repeatMode by viewModel.repeatMode.collectAsState()

    

    val sleepTimerTriggerTime by viewModel.sleepTimerTriggerTime.collectAsState()

    val sleepTimerPauseWhenSongEnd by viewModel.sleepTimerPauseWhenSongEnd.collectAsState()

    

    val isTimerActive = sleepTimerTriggerTime != -1L || sleepTimerPauseWhenSongEnd

    

    var sleepTimerTimeLeft by remember { mutableLongStateOf(0L) }



    LaunchedEffect(isTimerActive, sleepTimerTriggerTime) {

        if (isTimerActive && sleepTimerTriggerTime != -1L) {

            while (isActive) {

                sleepTimerTimeLeft = sleepTimerTriggerTime - System.currentTimeMillis()

                delay(1000)

            }

        }

    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    androidx.activity.compose.BackHandler(enabled = showLyrics) {
        showLyrics = false
    }

    var accentColor by remember { mutableStateOf(Color(0xFFB28D84)) } // Dusty rose/peach accent fallback
    var isWhiteArtwork by remember { mutableStateOf(false) } // true when artwork bottom is near-white
    val isArtworkDark by remember(accentColor) { derivedStateOf { accentColor.luminance() < 0.4f } }
    val lightThemeBgColor = if (isArtworkDark) accentColor else androidx.compose.ui.graphics.lerp(accentColor, Color.White, 0.7f)
    
    val gradientTopAlpha by animateFloatAsState(
        targetValue = if (showLyrics) 0.88f else 0.0f,
        animationSpec = spring(dampingRatio = 0.99f, stiffness = 300f),
        label = "gradientTopAlpha"
    )
    
    val textColor = if (isDarkTheme) Color.White else if (isWhiteArtwork) Color.White else if (isArtworkDark) Color.White else Color.Black
    val textAlpha = if (isDarkTheme) 0.7f else 0.6f

    val imageUrl = songToPlay?.artworkUri ?: songToPlay?.albumId?.let { "content://media/external/audio/albumart/$it" } ?: ""

    val canvasUrl by viewModel.canvasUrl.collectAsState()
    val canvasEnabled by viewModel.canvasEnabled.collectAsState()
    val canvasLoading by viewModel.canvasLoading.collectAsState()
    val canvasNotFound by viewModel.canvasNotFound.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Trigger canvas fetch whenever the playing track changes
    LaunchedEffect(songToPlay?.id) {
        val title = songToPlay?.title ?: return@LaunchedEffect
        val artist = songToPlay?.artist ?: return@LaunchedEffect
        val album = songToPlay?.album
        viewModel.fetchCanvasForTrack(title, artist, album)
    }

    @OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
    val sharedScope = LocalSharedTransitionScope.current
    @OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
    val navScope = LocalNavAnimatedVisibilityScope.current
    val jiggleSettings = LocalJigglePhysicsSettings.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {

                var totalDrag = 0f

                detectVerticalDragGestures(

                    onDragStart = { totalDrag = 0f },

                    onDragEnd = {

                        if (totalDrag < -100) {

                            onNavigateToQueue()

                        } else if (totalDrag > 100) {

                            // In lyrics mode swipe-down dismisses lyrics; otherwise exits the screen
                            if (showLyrics) showLyrics = false else onNavigateBack()

                        }

                    },

                    onVerticalDrag = { change, dragAmount ->

                        change.consume()

                        totalDrag += dragAmount

                    }

                )

            }

    ) {

        





        val density = androidx.compose.ui.platform.LocalDensity.current
        val imageRequest = ImageRequest.Builder(LocalContext.current)

            .data(imageUrl)

            .allowHardware(false)

            .build()



        // The background that Haze will read from

        Box(modifier = Modifier.fillMaxSize()) {

            // Blurred background for the whole screen

            AsyncImage(

                model = imageRequest,

                contentDescription = null,

                contentScale = ContentScale.Crop,

                onSuccess = { state ->

                    val drawable = state.result.drawable

                    val bitmap = (drawable as? BitmapDrawable)?.bitmap

                    if (bitmap != null) {

                        // Sample the bottom 20% strip of the artwork to get the color
                        // that actually sits at the artwork/controls boundary.
                        val stripTop = (bitmap.height * 0.80f).toInt().coerceAtLeast(0)
                        val bottomStrip = android.graphics.Bitmap.createBitmap(
                            bitmap, 0, stripTop, bitmap.width, bitmap.height - stripTop
                        )

                        // Average the pixels in the strip for a smooth representative colour
                        var rSum = 0L; var gSum = 0L; var bSum = 0L
                        val pixels = IntArray(bottomStrip.width * bottomStrip.height)
                        bottomStrip.getPixels(pixels, 0, bottomStrip.width, 0, 0, bottomStrip.width, bottomStrip.height)
                        pixels.forEach { px ->
                            rSum += android.graphics.Color.red(px)
                            gSum += android.graphics.Color.green(px)
                            bSum += android.graphics.Color.blue(px)
                        }
                        val count = pixels.size.toLong().coerceAtLeast(1L)
                        val avgColor = Color(
                            red   = (rSum / count).toInt().coerceIn(0, 255),
                            green = (gSum / count).toInt().coerceIn(0, 255),
                            blue  = (bSum / count).toInt().coerceIn(0, 255)
                        )
                        bottomStrip.recycle()

                        // If the bottom strip is very bright (near-white artwork edge),
                        // force a neutral grey so white controls stay legible — same
                        // approach Apple Music uses for bright artworks.
                        if (avgColor.luminance() > 0.65f) {
                            isWhiteArtwork = true
                            accentColor = Color(0xFF666666)
                        } else {
                            isWhiteArtwork = false
                            accentColor = avgColor
                        }

                    }

                },

                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp)

            )

            

            // Dark scrim over the blurred background to darken it

            Box(

                modifier = Modifier

                    .fillMaxSize()

                    .background(if (isDarkTheme) Color.Black.copy(alpha = if (isWhiteArtwork) 0.92f else 0.5f) else lightThemeBgColor.copy(alpha = if (isWhiteArtwork) 0.92f else 0.5f))

            )



            // Sharp image in the top half, fading out at the bottom — with optional canvas overlay

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
                    .clip(RoundedCornerShape(32.dp))
                    .clickable { showLyrics = true }
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.15f to Color.Black,
                                0.4f to Color.Black,
                                1.0f to Color.Transparent,
                                startY = 0f,
                                endY = size.height
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                // Static album art — always visible as base/fallback
                AsyncImage(

                    model = imageUrl,

                    contentDescription = "Album Art",

                    contentScale = ContentScale.Crop,

                    modifier = Modifier.fillMaxSize()
                )

                // Canvas artwork player — crossfades in over the static art
                val activeCanvasUrl = canvasUrl
                if (canvasEnabled && activeCanvasUrl != null) {
                    com.aeswox.arcmusic.ui.components.CanvasArtworkPlayer(
                        url = activeCanvasUrl,
                        isPlaying = isPlaying,
                        cacheDataSourceFactory = viewModel.canvasCacheManager.getCacheDataSourceFactory(),
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.05f)
                    )
                }

                // Badges
                androidx.compose.animation.AnimatedVisibility(
                    visible = canvasEnabled && (canvasLoading || canvasNotFound),
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (canvasLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else if (canvasNotFound) {
                            Icon(
                                imageVector = Icons.Outlined.VideocamOff,
                                contentDescription = "Canvas Unavailable",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            

            // Extra gradient scrim at the bottom to ensure text readability

            // Gradient scrim at the top (for lyrics readability over artwork)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = gradientTopAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Extra gradient scrim at the bottom to ensure text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent, 
                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = if (isWhiteArtwork) 0.85f else 0.4f), 
                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = if (isWhiteArtwork) 1.0f else 0.8f)
                            ),
                            startY = 0f
                        )
                    )
            )

        }

        

        // Content

        Column(

            modifier = Modifier

                .fillMaxSize()

                .systemBarsPadding()

        ) {

            // Top Bar removed as requested
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                AnimatedContent(
                    targetState = showLyrics,
                    transitionSpec = {
                        (fadeIn() togetherWith fadeOut()).using(
                            SizeTransform(
                                clip = false,
                                sizeAnimationSpec = { _, _ -> spring(dampingRatio = 0.99f, stiffness = 400f) }
                            )
                        )
                    },
                    label = "LyricsSwap"
                ) { isLyrics ->
                    if (isLyrics) {
                        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
                            ArcLyricsContent(
                                lyricsFraction = 1f,
                                textColor = textColor,
                                isDarkTheme = isDarkTheme,
                                accentColor = accentColor,
                                isWhiteArtwork = isWhiteArtwork,
                                imageUrl = imageUrl,
                                onDismiss = { showLyrics = false }
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // ── Centered Track Info ──────────────────────────────────────────────
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // More button on the left to balance the layout
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable { showOptionsSheet = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More",
                                        tint = textColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Title + artist centered in the remaining space
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = songToPlay?.title ?: "Unknown",
                                            style = MaterialTheme.typography.displaySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp
                                            ),
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        if (false /* songToPlay?.isExplicit == true */) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            com.aeswox.arcmusic.ExplicitBadge()
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = songToPlay?.artist ?: "Unknown",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textColor.copy(alpha = textAlpha),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                // Placeholder spacer to mirror the More button width so title stays centered
                                Spacer(modifier = Modifier.size(40.dp))
                            }

                            Spacer(modifier = Modifier.height(32.dp + (LocalConfiguration.current.screenHeightDp.dp * 0.1f)))

                            // ── Glassmorphic Controls Card (with seekbar) ───────────────────────
                            val isPlaying by viewModel.isPlaying.collectAsState()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(textColor.copy(alpha = 0.08f))
                                    .border(
                                        width = 1.dp,
                                        color = textColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // ── Wave Seekbar ──────────────────────────────────────
                                    ScrubberAndTimer(
                                        viewModel = viewModel,
                                        textColor = textColor,
                                        textAlpha = textAlpha,
                                        songToPlay = songToPlay,
                                        isPlayingProvider = { viewModel.isPlaying.value }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                    // Far Left — Favorite toggle
                                    IconButton(
                                        onClick = {
                                            songToPlay?.let { track ->
                                                viewModel.toggleFavorite(listOf(track.id), !track.isFavorite)
                                            }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (songToPlay?.isFavorite == true)
                                                Icons.Default.Favorite
                                            else
                                                Icons.Outlined.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (songToPlay?.isFavorite == true)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                textColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    // Previous
                                    IconButton(
                                        onClick = { viewModel.skipToPrevious() },
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FastRewind,
                                            contentDescription = "Previous",
                                            tint = textColor,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    // Play / Pause — larger tap target
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clickable { viewModel.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        com.aeswox.arcmusic.ui.components.PlayPauseMorphIcon(
                                            isPlaying = isPlaying,
                                            tint = textColor,
                                            modifier = Modifier.size(46.dp)
                                        )
                                    }

                                    // Next
                                    IconButton(
                                        onClick = { viewModel.skipToNext() },
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FastForward,
                                            contentDescription = "Next",
                                            tint = textColor,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    // Far Right — Shuffle
                                    IconButton(
                                        onClick = { viewModel.toggleShuffleMode() },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shuffle,
                                            contentDescription = "Shuffle",
                                            tint = if (shuffleEnabled)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                textColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        }
                    }
                }
            }
        }

        if (showSleepTimerDialog) {

            SleepTimerSheet(

                isActive = isTimerActive,

                timeLeft = sleepTimerTimeLeft,

                pauseWhenSongEnd = sleepTimerPauseWhenSongEnd,

                onDismiss = { showSleepTimerDialog = false },

                onStart = { minute ->

                    viewModel.startSleepTimer(minute)

                    showSleepTimerDialog = false

                },

                onClear = {

                    viewModel.clearSleepTimer()

                    showSleepTimerDialog = false

                }

            )

        }



        if (showDeviceSheet) {

            DeviceSheet(

                volume = deviceVolume,

                maxVolume = deviceMaxVolume,

                onVolumeChange = { viewModel.setDeviceVolume(it) },

                onDismiss = { showDeviceSheet = false }

            )

        }

    }



    if (showOptionsSheet) {

        ModalBottomSheet(

            onDismissRequest = { showOptionsSheet = false },

            sheetState = sheetState,

            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),

            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),

            scrimColor = Color.Black.copy(alpha = 0.4f),

            dragHandle = {

                Box(

                    modifier = Modifier

                        .padding(top = 16.dp, bottom = 8.dp)

                        .size(width = 32.dp, height = 4.dp)

                        .clip(CircleShape)

                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))

                )

            }

        ) {

            Column(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(bottom = 32.dp)

            ) {

                // Header

                Row(

                    modifier = Modifier

                        .fillMaxWidth()

                        .padding(horizontal = 24.dp)

                        .padding(bottom = 16.dp),

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    AsyncImage(

                        model = imageUrl,

                        contentDescription = null,

                        contentScale = ContentScale.Crop,

                        modifier = Modifier

                            .size(56.dp)

                            .clip(RoundedCornerShape(12.dp))

                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {

                        Text(

                            text = songToPlay?.title ?: "Unknown",

                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),

                            color = MaterialTheme.colorScheme.onSurface

                        )

                        Text(

                            text = songToPlay?.artist ?: "Unknown Artist",

                            style = MaterialTheme.typography.bodyMedium,

                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

                        )

                    }

                }

                

                Divider(

                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),

                    modifier = Modifier.padding(horizontal = 24.dp)

                )

                

                Spacer(modifier = Modifier.height(8.dp))

                

                // Options

                val options = listOf(

                    Triple(Icons.Default.PlaylistAdd, "Add to playlist", false),

                    Triple(if (songToPlay?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder, if (songToPlay?.isFavorite == true) "Remove from favorites" else "Add to favorites", false),

                    Triple(Icons.Default.Album, "Go to album", false),

                    Triple(Icons.Default.Person, "Go to artist", false),

                    Triple(Icons.Default.Share, "Share", false),

                    Triple(Icons.Default.Info, "Details", false)

                )

                

                options.forEach { (icon, title, hasToggle) ->

                    Row(

                        modifier = Modifier

                            .fillMaxWidth()

                            .jellyClick { 

                                showOptionsSheet = false 

                                if (title == "Add to playlist") {

                                    showAddToPlaylistSheet = true

                                } else if (title == "Add to favorites" || title == "Remove from favorites") {

                                    songToPlay?.let { track ->

                                        viewModel.toggleFavorite(listOf(track.id), !track.isFavorite)

                                    }

                                } else if (title == "Go to album") {

                                    songToPlay?.let { track ->

                                        onNavigateToAlbum(track.album)

                                    }

                                } else if (title == "Go to artist") {

                                    songToPlay?.let { track ->

                                        onNavigateToArtist(track.artist)

                                    }

                                } else if (title == "Share") {

                                    songToPlay?.let { track ->

                                        onNavigateToShare("track", track.id)

                                    }

                                } else if (title == "Details") {

                                    showDetailsDialog = true

                                }

                            }

                            .padding(horizontal = 24.dp, vertical = 16.dp),

                        verticalAlignment = Alignment.CenterVertically,

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Icon(

                                imageVector = icon,

                                contentDescription = title,

                                tint = MaterialTheme.colorScheme.onSurfaceVariant,

                                modifier = Modifier.size(24.dp)

                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(

                                text = title,

                                style = MaterialTheme.typography.bodyLarge,

                                color = MaterialTheme.colorScheme.onSurface

                            )

                        }

                        if (hasToggle) {

                            var isDownloaded by remember { mutableStateOf(true) }

                            Switch(

                                checked = isDownloaded,

                                onCheckedChange = { isDownloaded = it },

                                colors = SwitchDefaults.colors(

                                    checkedThumbColor = Color.White,

                                    checkedTrackColor = MaterialTheme.colorScheme.primary,

                                    uncheckedThumbColor = Color.Gray,

                                    uncheckedTrackColor = Color.DarkGray

                                ),

                                modifier = Modifier.scale(0.8f)

                            )

                        }

                    }

                }

            }

        }

    }



    if (showAddToPlaylistSheet) {

        val trackIds = songToPlay?.let { listOf(it.id) } ?: emptyList()

        AddToPlaylistSheet(trackIds = trackIds, onDismissRequest = { showAddToPlaylistSheet = false })

    }



    if (showDetailsDialog) {

        songToPlay?.let { track ->

            val durationMs = track.durationMs

            val minutes = durationMs / 1000 / 60

            val seconds = (durationMs / 1000 % 60).toString().padStart(2, '0')

            val duration = "$minutes:$seconds"

            val sizeMb = track.fileSizeBytes / 1024 / 1024

            

            androidx.compose.material3.AlertDialog(

                onDismissRequest = { showDetailsDialog = false },

                title = { Text("Track Details", style = MaterialTheme.typography.titleLarge) },

                text = {

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        Text("Title: ${track.title}")

                        Text("Artist: ${track.artist}")

                        Text("Album: ${track.album}")

                        Text("Duration: $duration")

                        Text("Size: $sizeMb MB")

                        track.bitrate?.let { Text("Bitrate: $it kbps") }

                        track.sampleRate?.let { Text("Sample Rate: ${it / 1000f} kHz") }

                        track.bitDepth?.let { Text("Bit Depth: $it bit") }

                        track.codec?.let { Text("Codec: ${it.uppercase()}") }

                        Text("File: ${track.filePath}")

                    }

                },

                confirmButton = {

                    androidx.compose.material3.TextButton(onClick = { showDetailsDialog = false }) {

                        Text("Close")

                    }

                }

            )

        }

    }

}



@Composable
fun CustomPauseIcon(color: Color, modifier: Modifier = Modifier) {

    Canvas(modifier = modifier) {

        val barWidth = size.width * 0.35f

        val cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)

        drawRoundRect(

            color = color,

            topLeft = Offset(0f, 0f),

            size = androidx.compose.ui.geometry.Size(barWidth, size.height),

            cornerRadius = cornerRadius

        )

        drawRoundRect(

            color = color,

            topLeft = Offset(size.width - barWidth, 0f),

            size = androidx.compose.ui.geometry.Size(barWidth, size.height),

            cornerRadius = cornerRadius

        )

    }

}



@Composable

fun CustomListIcon(color: Color, modifier: Modifier = Modifier) {

    Canvas(modifier = modifier) {

        val dotRadius = size.height * 0.08f

        val lineThickness = size.height * 0.12f

        val lineLength = size.width * 0.65f

        val spacing = size.height * 0.35f

        val startY = size.height * 0.15f

        

        for (i in 0..2) {

            val y = startY + (i * spacing)

            drawCircle(color = color, radius = dotRadius, center = Offset(dotRadius * 1.5f, y))

            drawLine(

                color = color,

                start = Offset(dotRadius * 4.5f, y),

                end = Offset(dotRadius * 4.5f + lineLength, y),

                strokeWidth = lineThickness,

                cap = StrokeCap.Round

            )

        }

    }
}

/**

 * FADE style: renders a single lyric line with:
 *  - Bold weight on every word (active and inactive alike)
 *  - Inactive lines: opacity-only dimming, ZERO text blur
 *  - Active line: cumulative word-fill — every word whose [SyncedWord.time] <=
 *    [currentPositionMsProvider] stays bright and never reverts for the
 *    duration of that line. Words not yet reached are dim.
 *  - Inactive lines' opacity animates smoothly with the existing 350ms tween.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FadeLyricLine(
    lineIndex: Int,
    syncedLine: SyncedLine?,
    plainWords: List<String>,
    activeLineIndexProvider: () -> Int,
    currentPositionProvider: () -> Long,
    listState: androidx.compose.foundation.lazy.LazyListState,
    textColor: Color,
    fadeSteepness: Float = 1.2f,
    fadeScaleCeiling: Float = 0.85f,
    distanceSizing: Boolean = true,
    baseFontSize: androidx.compose.ui.unit.TextUnit = 32.sp
) {
    val isActive by remember { derivedStateOf { lineIndex == activeLineIndexProvider() } }
    val currentPosition = if (isActive) currentPositionProvider() else 0L

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                val layoutInfo = listState.layoutInfo
                val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == lineIndex }
                
                if (itemInfo != null) {
                    val viewportHeight = layoutInfo.viewportSize.height.toFloat()
                    
                    // In LazyColumn, itemInfo.offset is 0 when the item is perfectly aligned with the viewport start
                    // (which happens automatically when animateScrollToItem is called, placing it right below the top content padding).
                    // Therefore, the item is perfectly at the focal point when its offset is 0.
                    val distance = kotlin.math.abs(itemInfo.offset).toFloat()
                    
                    val maxDistance = viewportHeight * 0.5f
                    val progress = (distance / maxDistance).coerceIn(0f, 1f)
                    
                    val maxScaleForState = if (isActive) 1f else fadeScaleCeiling
                    val maxAlphaForState = if (isActive) 1f else 0.7f
                    
                    val targetScale = if (distanceSizing) {
                        when {
                            progress < 0.1f -> 1f - (progress * 1.5f)
                            else -> fadeScaleCeiling - ((progress - 0.1f) * 0.4f)
                        }.coerceIn(0.4f, maxScaleForState)
                    } else {
                        1f
                    }
                    
                    val targetAlpha = when {
                        progress < 0.2f -> 1f - (progress * 1.5f)
                        else -> 0.7f - ((progress - 0.2f) * fadeSteepness) // Fades to 0 right before the controls
                    }.coerceIn(0.0f, maxAlphaForState)
                    
                    scaleX = targetScale
                    scaleY = targetScale
                    alpha = targetAlpha
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f) // Scale from left-center
                } else {
                    alpha = 0f
                }
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val words = syncedLine?.words
        if (!words.isNullOrEmpty()) {
            // Word-timed path: continuous interpolated fill driven by exact timestamp.
            words.forEach { syncedWord ->
                val wordAlpha = when {
                    !isActive -> 1f   // Inactive lines handled purely by lineAlpha fade
                    currentPosition >= syncedWord.time -> 1f // Already sung -> full brightness
                    else -> {
                        // Smoothly light up over the 250ms before the word's exact start time
                        val timeUntilWord = syncedWord.time - currentPosition
                        if (timeUntilWord < 250) {
                            val progress = 1f - (timeUntilWord / 250f)
                            0.4f + (progress * 0.6f)
                        } else {
                            0.4f // Unsung words stay dim on the active line
                        }
                    }
                }
                
                Text(
                    text = syncedWord.word,
                    color = textColor.copy(alpha = wordAlpha),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = baseFontSize,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            // No word timing — plain text words, all at full alpha (line controls dimming).
            plainWords.forEach { word ->
                Text(
                    text = word,
                    color = textColor,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = baseFontSize,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun ScrubberAndTimer(
    viewModel: MusicViewModel,
    textColor: Color,
    textAlpha: Float,
    songToPlay: Track?,
    showBadges: Boolean = true,
    isPlayingProvider: (() -> Boolean)? = null
) {
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isPlayingState by viewModel.isPlaying.collectAsState()
    val isPlaying = isPlayingProvider?.invoke() ?: isPlayingState

    val seekbarBaselineHeight by viewModel.seekbarBaselineHeight.collectAsState()
    val seekbarWaveMaxAmp by viewModel.seekbarWaveMaxAmp.collectAsState()
    val seekbarCycleLength by viewModel.seekbarCycleLength.collectAsState()
    val seekbarShadowOffset by viewModel.seekbarShadowOffset.collectAsState()
    val seekbarShadowOpacity by viewModel.seekbarShadowOpacity.collectAsState()
    val seekbarPrimaryOpacity by viewModel.seekbarPrimaryOpacity.collectAsState()
    val seekbarThumbRadius by viewModel.seekbarThumbRadius.collectAsState()
    val seekbarUnplayedStroke by viewModel.seekbarUnplayedStroke.collectAsState()
    val seekbarBloomDuration by viewModel.seekbarBloomDuration.collectAsState()

    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val progress = if (isSeeking) {
        sliderPosition
    } else {
        if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    }

    // Animate wave phase — continuously advances when playing
    val infiniteTransition = rememberInfiniteTransition(label = "wavePhase")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Animate amplitude between 0 (paused) and 1 (playing)
    val targetAmplitude = if (isPlaying && !isSeeking) 1f else 0f
    val waveAmplitude by animateFloatAsState(
        targetValue = targetAmplitude,
        animationSpec = tween(durationMillis = seekbarBloomDuration.toInt(), easing = FastOutSlowInEasing),
        label = "waveAmplitude"
    )

    // Wave seekbar canvas
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    viewModel.seekTo(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isSeeking = true
                        sliderPosition = (it.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        isSeeking = false
                        viewModel.seekTo(sliderPosition)
                    },
                    onDragCancel = { isSeeking = false }
                ) { change, dragAmount ->
                    change.consume()
                    sliderPosition = (sliderPosition + dragAmount / size.width).coerceIn(0f, 1f)
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val playedWidth = w * progress
        val thumbRadiusPx = seekbarThumbRadius.dp.toPx()

        // --- Geometry ---
        // The track sits vertically centered. We define:
        //   baselineHeight: the thick solid bar always visible for the played region
        //   waveMaxAmp:     extra height the wave crests add above the baseline top
        // The bottom edge is always flat; the top edge undulates.
        val baselineHeightPx = seekbarBaselineHeight.dp.toPx()
        val waveMaxAmpPx = seekbarWaveMaxAmp.dp.toPx()
        val totalMaxHeight = baselineHeightPx + waveMaxAmpPx

        // Center everything vertically in the canvas
        val bottomY = (h + totalMaxHeight) / 2f  // flat bottom edge of the track
        val baselineTopY = bottomY - baselineHeightPx  // top of the solid baseline (= trough of wave)

        // Frequency: physical cycle length
        val cycleLengthPx = seekbarCycleLength.dp.toPx()
        val frequency = 2f * Math.PI.toFloat() / cycleLengthPx

        // Unplayed track: thin flat line, right of thumb
        val unplayedCenterY = bottomY - baselineHeightPx / 2f
        drawLine(
            color = textColor.copy(alpha = 0.25f),
            start = Offset(playedWidth.coerceAtMost(w), unplayedCenterY),
            end = Offset(w, unplayedCenterY),
            strokeWidth = seekbarUnplayedStroke.dp.toPx(),
            cap = StrokeCap.Round
        )

        // --- Draw played region ---
        if (playedWidth > 1f) {
            val clampedWidth = playedWidth.coerceAtMost(w)
            val steps = clampedWidth.toInt().coerceAtLeast(2)

            // Top-edge Y for a given x along the played region.
            // Amplitude is tapered: sin(π·t) envelope so the wave fades in from
            // the left and tapers back to flat approaching the thumb.
            // The wave only goes UPWARD from baselineTopY (never below it).
            fun waveTopY(x: Float, phaseOffset: Float): Float {
                val t = (x / clampedWidth).coerceIn(0f, 1f)
                val taper = sin(Math.PI.toFloat() * t).coerceAtLeast(0f)
                val amp = waveMaxAmpPx * waveAmplitude * taper
                // sin oscillates -1..1 but we only want upward motion from baseline
                // Map it so 0=baselineTopY and peak goes up by amp
                val sinVal = (1f - sin(frequency * x + wavePhase + phaseOffset)) / 2f  // 0..1 range
                return baselineTopY - amp * sinVal
            }

            // --- Layer 2 (shadow) — phase-shifted, dimmer ---
            val path2 = Path()
            path2.moveTo(0f, bottomY)
            for (i in 0..steps) {
                val x = (i.toFloat() / steps) * clampedWidth
                path2.lineTo(x, waveTopY(x, seekbarShadowOffset))
            }
            path2.lineTo(clampedWidth, bottomY)
            path2.close()
            drawPath(path = path2, color = textColor.copy(alpha = seekbarShadowOpacity))

            // Round the ends of the shadow wave
            drawCircle(color = textColor.copy(alpha = seekbarShadowOpacity), radius = baselineHeightPx / 2f, center = Offset(0f, bottomY - baselineHeightPx / 2f))
            drawCircle(color = textColor.copy(alpha = seekbarShadowOpacity), radius = baselineHeightPx / 2f, center = Offset(clampedWidth, bottomY - baselineHeightPx / 2f))

            // --- Layer 1 (foreground primary wave) ---
            val path1 = Path()
            path1.moveTo(0f, bottomY)
            for (i in 0..steps) {
                val x = (i.toFloat() / steps) * clampedWidth
                path1.lineTo(x, waveTopY(x, 0f))
            }
            path1.lineTo(clampedWidth, bottomY)
            path1.close()
            drawPath(path = path1, color = textColor.copy(alpha = seekbarPrimaryOpacity))
            
            // Round the ends of the primary wave
            drawCircle(color = textColor.copy(alpha = seekbarPrimaryOpacity), radius = baselineHeightPx / 2f, center = Offset(0f, bottomY - baselineHeightPx / 2f))
            drawCircle(color = textColor.copy(alpha = seekbarPrimaryOpacity), radius = baselineHeightPx / 2f, center = Offset(clampedWidth, bottomY - baselineHeightPx / 2f))
        }

        // --- Thumb circle ---
        val thumbCx = playedWidth.coerceIn(thumbRadiusPx, w - thumbRadiusPx)
        val thumbCy = bottomY - baselineHeightPx / 2f
        drawCircle(
            color = textColor,
            radius = thumbRadiusPx,
            center = Offset(thumbCx, thumbCy)
        )
    }


    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val currentPosMs = if (isSeeking) (sliderPosition * duration).toLong() else currentPosition
        Text(
            text = formatDuration(currentPosMs),
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = textAlpha)
        )
        // Format Badges
        if (showBadges) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val codec = songToPlay?.codec?.lowercase() ?: ""
                val path = songToPlay?.filePath?.lowercase() ?: ""
                val isAtmos = codec.contains("eac3") || codec.contains("ac3") || path.endsWith(".eac3") || path.endsWith(".ac3") || (path.endsWith(".m4a") && codec.contains("ec-3"))

                if (isAtmos) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_dolby_atmos),
                        contentDescription = "Dolby Atmos",
                        modifier = Modifier.height(14.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(textColor)
                    )
                } else {
                    val isLosslessCodec = codec.contains("flac") || codec.contains("alac") || codec.contains("ape") || codec.contains("dsd") || path.endsWith(".flac") || path.endsWith(".wav") || codec.contains("wav")
                    if (isLosslessCodec) {
                        val bitDepth = songToPlay?.bitDepth ?: 16
                        val sampleRateKhz = (songToPlay?.sampleRate ?: 0) / 1000f
                        if (bitDepth >= 24 || sampleRateKhz >= 48f) {
                            HiResLogo(color = textColor)
                        } else if (bitDepth >= 16) {
                            LosslessLogo(color = textColor)
                        }
                    }
                }
            }
        }

        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = textAlpha)
        )
    }
}


/**
 * Lyrics content for Arc style.
 *
 * Rendered directly inside the main now-playing Box so every element is
 * part of the SAME composition — not a separate screen. The [lyricsFraction]
 * (0 = normal, 1 = lyrics) is passed in from the parent and drives the
 * alpha of the entire layer. This gives a true cross-fade/morph feel.
 *
 * There is intentionally NO background here: the expanding scrim in the
 * parent already provides the dark overlay. Adding another background would
 * make it look like a new surface is sliding in.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ArcLyricsContent(
    lyricsFraction: Float = 1f,
    textColor: Color = Color.White,
    isDarkTheme: Boolean = true,
    accentColor: Color = Color(0xFFB28D84),
    isWhiteArtwork: Boolean = false,
    imageUrl: String = "",
    onDismiss: () -> Unit = {}
) {
    val viewModel: MusicViewModel = hiltViewModel()
    val currentlyPlayingEntity by viewModel.currentlyPlaying.collectAsState()
    val randomPicks       by viewModel.randomPicks.collectAsState()
    val libraryTracks     by viewModel.libraryTracks.collectAsState()

    val rawSongToPlay = currentlyPlayingEntity ?: randomPicks.firstOrNull()
    val songToPlay    = libraryTracks.find { it.id == rawSongToPlay?.id } ?: rawSongToPlay

    // imageUrl is passed in from the parent — no need to re-derive it here

    // ── Lyrics data ──────────────────────────────────────────────────────────
    val lyricsData             by viewModel.lyricsUiState.collectAsState()
    val lyricsDisplayStyle     by viewModel.lyricsDisplayStyle.collectAsState()
    val lyricsShowControls     by viewModel.lyricsShowControls.collectAsState()
    val lyricsFadeSteepness    by viewModel.lyricsFadeSteepness.collectAsState()
    val lyricsFadeScaleCeiling by viewModel.lyricsFadeScaleCeiling.collectAsState()
    val lyricsFadeDistanceSizing by viewModel.lyricsFadeDistanceSizing.collectAsState()
    val lyricsBlurRadius       by viewModel.lyricsBlurRadius.collectAsState()
    val lyricsBlurDimming      by viewModel.lyricsBlurDimming.collectAsState()

    val rawSyncedLines = lyricsData?.synced
    val plainLines     = lyricsData?.plain
    val duration       by viewModel.duration.collectAsState()

    // Enrich synced lines: insert "● ● ●" placeholders for long gaps (same as Fruit screen)
    val syncedLines = remember(rawSyncedLines, duration) {
        if (rawSyncedLines.isNullOrEmpty()) return@remember null
        val enriched     = mutableListOf<SyncedLine>()
        val gapThreshold = 10_000
        if (rawSyncedLines.first().time > gapThreshold)
            enriched.add(SyncedLine(time = 2000, line = "\u25CF \u25CF \u25CF"))
        for (i in 0 until rawSyncedLines.size - 1) {
            enriched.add(rawSyncedLines[i])
            if (rawSyncedLines[i + 1].time - rawSyncedLines[i].time > gapThreshold)
                enriched.add(SyncedLine(time = rawSyncedLines[i].time + 5000, line = "\u25CF \u25CF \u25CF"))
        }
        if (rawSyncedLines.isNotEmpty()) {
            enriched.add(rawSyncedLines.last())
            if (duration > 0 && duration - rawSyncedLines.last().time > gapThreshold)
                enriched.add(SyncedLine(time = rawSyncedLines.last().time + 5000, line = "\u25CF \u25CF \u25CF"))
        }
        enriched.toList()
    }

    val currentPositionState = viewModel.currentPlaybackPosition.collectAsState()
    val linesToRender = remember(syncedLines, plainLines) {
        syncedLines?.map { it.line } ?: plainLines ?: listOf("No lyrics available")
    }

    // ── Active-line tracking ─────────────────────────────────────────────────
    var activeLineIndex by remember { mutableIntStateOf(0) }
    var activeWordIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(syncedLines) {
        viewModel.currentPlaybackPosition.collect { pos ->
            if (!syncedLines.isNullOrEmpty()) {
                val lastMatchIndex = syncedLines.indexOfLast { it.time <= pos }
                val newLineIndex = if (lastMatchIndex >= 0) {
                    val matchTime = syncedLines[lastMatchIndex].time
                    syncedLines.indexOfFirst { it.time == matchTime }
                } else 0
                if (activeLineIndex != newLineIndex) activeLineIndex = newLineIndex
                if (newLineIndex in syncedLines.indices) {
                    val line = syncedLines[newLineIndex]
                    if (!line.words.isNullOrEmpty()) {
                        val newWordIndex = line.words.indexOfLast { it.time <= pos }.coerceAtLeast(0)
                        if (activeWordIndex != newWordIndex) activeWordIndex = newWordIndex
                    } else {
                        if (activeWordIndex != -1) activeWordIndex = -1
                    }
                }
            } else {
                if (activeLineIndex != -1) activeLineIndex = -1
                if (activeWordIndex != -1) activeWordIndex = -1
            }
        }
    }

    val activeLineIndexProvider = remember { { activeLineIndex } }
    val activeWordIndexProvider = remember { { activeWordIndex } }

    // ── Scroll state ─────────────────────────────────────────────────────────
    val listState = rememberLazyListState()

    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in 0 until linesToRender.size) {
            val visibleItem = listState.layoutInfo.visibleItemsInfo.find { it.index == activeLineIndex }
            if (visibleItem != null && visibleItem.offset != 0) {
                listState.animateScrollBy(
                    value = visibleItem.offset.toFloat(),
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 50f)
                )
            } else {
                listState.animateScrollToItem(activeLineIndex)
            }
        }
    }

    val lightThemeBgColor = if (accentColor.luminance() < 0.4f) accentColor
                            else androidx.compose.ui.graphics.lerp(accentColor, Color.White, 0.7f)
    val bgColor = if (isDarkTheme) Color.Black else lightThemeBgColor

    val listSpacing   = if (lyricsDisplayStyle == LyricsDisplayStyle.FADE) 42.dp else 28.dp
    val bottomPadding = if (lyricsShowControls) 300.dp else 120.dp

    // The entire lyrics layer uses lyricsFraction for alpha — this is what makes
    // the transition feel like elements morphing in place, not a new screen fading in.
    Box(modifier = Modifier
        .fillMaxSize()
        .graphicsLayer { alpha = lyricsFraction }
    ) {


        // ── Lyrics list ──────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top   = 140.dp,
                bottom = bottomPadding,
                start = 28.dp,
                end   = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(listSpacing)
        ) {
            itemsIndexed(linesToRender) { lineIndex, line ->
                val words = remember(lineIndex, syncedLines, line) {
                    if (!syncedLines.isNullOrEmpty() && !syncedLines[lineIndex].words.isNullOrEmpty())
                        syncedLines[lineIndex].words!!.map { it.word }
                    else
                        line.split(" ")
                }
                if (lyricsDisplayStyle == LyricsDisplayStyle.FADE) {
                    FadeLyricLine(
                        lineIndex               = lineIndex,
                        syncedLine              = syncedLines?.getOrNull(lineIndex),
                        plainWords              = words,
                        activeLineIndexProvider = activeLineIndexProvider,
                        currentPositionProvider = { currentPositionState.value },
                        listState               = listState,
                        textColor               = textColor,
                        fadeSteepness           = lyricsFadeSteepness,
                        fadeScaleCeiling        = lyricsFadeScaleCeiling,
                        distanceSizing          = lyricsFadeDistanceSizing
                    )
                } else {
                    LyricLine(
                        line                   = line,
                        words                  = words,
                        lineIndex              = lineIndex,
                        activeLineIndexProvider = activeLineIndexProvider,
                        activeWordIndexProvider = activeWordIndexProvider,
                        textColor              = textColor,
                        blurRadiusMax          = lyricsBlurRadius,
                        blurDimming            = lyricsBlurDimming
                    )
                }
            }
        }



        // ── Docked bottom controls (spring entry) ────────────────────────────
        if (lyricsShowControls) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                bgColor.copy(alpha = 0.65f),
                                bgColor.copy(alpha = 0.96f),
                                bgColor
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(top = 56.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {

                    // Title + fav
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text  = songToPlay?.title ?: "Unknown",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 26.sp
                                    ),
                                    color    = textColor,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (false /* songToPlay?.isExplicit == true */) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    com.aeswox.arcmusic.ExplicitBadge()
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text  = songToPlay?.artist ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.15f))
                                .clickable {
                                    songToPlay?.let { track ->
                                        viewModel.toggleFavorite(listOf(track.id), !track.isFavorite)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (songToPlay?.isFavorite == true)
                                    Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint     = if (songToPlay?.isFavorite == true) Color.White else textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    ScrubberAndTimer(
                        viewModel  = viewModel,
                        textColor  = textColor,
                        textAlpha  = 0.7f,
                        songToPlay = songToPlay,
                        showBadges = false
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Playback controls
                    val isPlayingArc by viewModel.isPlaying.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick  = { viewModel.skipToPrevious() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.FastRewind,
                                contentDescription = "Previous",
                                tint               = textColor,
                                modifier           = Modifier.size(52.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clickable { viewModel.togglePlayPause() },
                            contentAlignment = Alignment.Center
                        ) {
                            com.aeswox.arcmusic.ui.components.PlayPauseMorphIcon(
                                isPlaying = isPlayingArc,
                                tint      = textColor,
                                modifier  = Modifier.size(50.dp)
                            )
                        }
                        IconButton(
                            onClick  = { viewModel.skipToNext() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Rounded.FastForward,
                                contentDescription = "Next",
                                tint               = textColor,
                                modifier           = Modifier.size(52.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CustomLyricsIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeW = size.width * 0.08f
        val w = size.width
        val h = size.height
        val corner = w * 0.2f
        
        val path = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = strokeW, top = strokeW, right = w - strokeW, bottom = h * 0.8f,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner)
                )
            )
            moveTo(w * 0.65f, h * 0.8f)
            lineTo(w * 0.65f, h - strokeW)
            lineTo(w * 0.85f, h * 0.8f)
        }
        
        drawPath(path, color, style = Stroke(width = strokeW, join = androidx.compose.ui.graphics.StrokeJoin.Round, cap = StrokeCap.Round))
        
        val q1 = Offset(w * 0.35f, h * 0.38f)
        val q2 = Offset(w * 0.65f, h * 0.38f)
        val r = w * 0.07f
        
        drawCircle(color, r, q1)
        drawCircle(color, r, q2)
        
        val tails = Path().apply {
            moveTo(q1.x + r, q1.y)
            quadraticBezierTo(q1.x + r, q1.y + r * 2.5f, q1.x - r, q1.y + r * 3f)
            
            moveTo(q2.x + r, q2.y)
            quadraticBezierTo(q2.x + r, q2.y + r * 2.5f, q2.x - r, q2.y + r * 3f)
        }
        drawPath(tails, color, style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round))
    }
}


@Composable
fun LyricWord(
    word: String,
    isHighlighted: Boolean,
    isLineActive: Boolean,
    textColor: Color,
    baseFontSize: Float = 24f
) {
    val wordAlpha by animateFloatAsState(
        targetValue = if (isHighlighted || !isLineActive) 1f else 0.55f,
        animationSpec = tween(durationMillis = 200),
        label = "wordAlpha"
    )
    // Use fontSize animation instead of graphicsLayer scale so Compose measures
    // the text at its real size and words never overflow their layout bounds.
    val wordFontSize by animateFloatAsState(
        targetValue = if (isHighlighted) baseFontSize + 2f else baseFontSize,
        animationSpec = tween(durationMillis = 200),
        label = "wordFontSize"
    )

    Text(
        text = word,
        color = textColor.copy(alpha = wordAlpha),
        style = MaterialTheme.typography.displayMedium.copy(
            fontSize = wordFontSize.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LyricLine(
    line: String,
    words: List<String>,
    lineIndex: Int,
    activeLineIndexProvider: () -> Int,
    activeWordIndexProvider: () -> Int,
    textColor: Color,
    blurRadiusMax: Float = 10f,
    blurDimming: Float = 0.28f
) {
    val distance by remember {
        derivedStateOf {
            kotlin.math.abs(lineIndex - activeLineIndexProvider())
        }
    }
    val isActive by remember { derivedStateOf { distance == 0 } }
    val isNear by remember { derivedStateOf { distance == 1 } }
    val isFar by remember { derivedStateOf { distance >= 3 } }

    val targetAlpha = when {
        isActive -> 1f
        isNear   -> 0.55f
        isFar    -> (blurDimming * 0.42f)
        else     -> blurDimming
    }
    val targetPadding = when {
        isActive -> 28.dp
        isNear   -> 12.dp
        else     -> 8.dp
    }

    val lineAnimSpec = tween<Float>(durationMillis = 300)
    val paddingAnimSpec = tween<androidx.compose.ui.unit.Dp>(durationMillis = 300)

    val lineAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = lineAnimSpec,
        label = "alpha"
    )
    val linePadding by animateDpAsState(
        targetValue = targetPadding,
        animationSpec = paddingAnimSpec,
        label = "padding"
    )

    // Blur non-active lines; active line is never blurred.
    val targetBlur = if (distance > 0) {
        (distance * (blurRadiusMax * 0.25f)).coerceAtMost(blurRadiusMax).dp
    } else 0.dp
    val blurRadius by animateDpAsState(
        targetValue = targetBlur,
        animationSpec = tween(durationMillis = 400),
        label = "lineBlur"
    )

    // Animate font size at the line level instead of graphicsLayer scale.
    // This way Compose measures the FlowRow at the actual rendered size so
    // words never escape their layout bounds.
    val targetFontSize = when {
        isActive -> 24f
        isNear   -> 21f
        else     -> 18f
    }
    val lineFontSize by animateFloatAsState(
        targetValue = targetFontSize,
        animationSpec = lineAnimSpec,
        label = "fontSize"
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = linePadding)
            .graphicsLayer { alpha = lineAlpha }
            .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        words.forEachIndexed { wordIndex, word ->
            val isHighlighted = isActive && wordIndex == activeWordIndexProvider()
            LyricWord(
                word = word,
                isHighlighted = isHighlighted,
                isLineActive = isActive,
                textColor = textColor,
                baseFontSize = lineFontSize
            )
        }
    }
}
