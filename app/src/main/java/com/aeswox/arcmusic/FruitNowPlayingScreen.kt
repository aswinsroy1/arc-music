@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
package com.aeswox.arcmusic

import androidx.compose.animation.SharedTransitionScope
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

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage

import coil.request.ImageRequest

import androidx.compose.ui.platform.LocalContext


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



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun FruitNowPlayingScreen(

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

    

    var showLyrics by remember { mutableStateOf(false) }
    
    androidx.activity.compose.BackHandler(enabled = showLyrics) {
        showLyrics = false
    }

    var showOptionsSheet by remember { mutableStateOf(false) }

    var showAddToPlaylistSheet by remember { mutableStateOf(false) }

    var showDetailsDialog by remember { mutableStateOf(false) }

    var showSleepTimerDialog by remember { mutableStateOf(false) }

    var showDeviceSheet by remember { mutableStateOf(false) }

    

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

    var accentColor by remember { mutableStateOf(Color(0xFFB28D84)) } // Dusty rose/peach accent fallback
    var isWhiteArtwork by remember { mutableStateOf(false) } // true when artwork bottom is near-white
    val isArtworkDark by remember(accentColor) { derivedStateOf { accentColor.luminance() < 0.4f } }
    val lightThemeBgColor = if (isArtworkDark) accentColor else androidx.compose.ui.graphics.lerp(accentColor, Color.White, 0.7f)
    
    val scrimHeightFraction by animateFloatAsState(targetValue = if (showLyrics) 1f else 0.7f, label = "height")
    val scrimStartAlpha by animateFloatAsState(targetValue = if (showLyrics) 0.0f else 0f, label = "startAlpha")
    val midAlphaRatio by animateFloatAsState(targetValue = if (showLyrics) 0.0f else if (isWhiteArtwork) 0.85f else 0.4f, label = "midAlphaRatio")
    val endAlphaRatio by animateFloatAsState(targetValue = if (showLyrics) 0.1f else if (isWhiteArtwork) 1.0f else 0.8f, label = "endAlphaRatio")
    val baseScrimAlpha by animateFloatAsState(targetValue = if (showLyrics) 0.15f else if (isWhiteArtwork) 0.92f else 0.5f, label = "baseScrim")
    val sharpImageAlpha by animateFloatAsState(targetValue = if (showLyrics) 0f else 1f, label = "sharpImageAlpha")
    val controlsAlpha by animateFloatAsState(targetValue = if (showLyrics) 0f else 1f, label = "controlsAlpha")
    
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

                            onNavigateBack()

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

                    .background(if (isDarkTheme) Color.Black.copy(alpha = baseScrimAlpha) else lightThemeBgColor.copy(alpha = 0.5f))

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
                        alpha = sharpImageAlpha
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

            Box(

                modifier = Modifier

                    .fillMaxWidth()

                    .fillMaxHeight(scrimHeightFraction)

                    .align(Alignment.BottomCenter)

                    .background(

                        Brush.verticalGradient(

                            colors = listOf(

                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = scrimStartAlpha), 

                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = midAlphaRatio), 

                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = endAlphaRatio)

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

                .padding(horizontal = 24.dp)

        ) {

            // Top Bar removed as requested
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

                // Controls Column

                Column(

                    modifier = Modifier

                        .fillMaxSize()

                        .graphicsLayer { alpha = controlsAlpha }

                ) {

                    Spacer(modifier = Modifier.weight(1f))

                    

                    // Title and Actions

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = songToPlay?.title ?: "Unknown",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp
                                    ),
                                    color = textColor,
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
                                text = songToPlay?.artist ?: "Unknown",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor.copy(alpha = textAlpha),
                                maxLines = 1
                            )

                        }

                        Row(

                            horizontalArrangement = Arrangement.spacedBy(16.dp),

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Box(

                                modifier = Modifier

                                    .size(48.dp)

                                    .clip(CircleShape)

                                    .background(textColor.copy(alpha = 0.15f))

                                    .clickable { showOptionsSheet = true },

                                contentAlignment = Alignment.Center

                            ) {

                                Icon(

                                    imageVector = Icons.Default.MoreVert,

                                    contentDescription = "More",

                                    tint = textColor,

                                    modifier = Modifier.size(24.dp)

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

                                    imageVector = if (songToPlay?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,

                                    contentDescription = "Favorite",

                                    tint = if (songToPlay?.isFavorite == true) Color.White else textColor,

                                    modifier = Modifier.size(24.dp)

                                )

                            }

                        }

                    }

                    

                    Spacer(modifier = Modifier.height(32.dp))

                    

                    ScrubberAndTimer(viewModel = viewModel, textColor = textColor, textAlpha = textAlpha, songToPlay = songToPlay)

                    

                    Spacer(modifier = Modifier.height(48.dp))

                    

                    // Controls

                    val isPlaying by viewModel.isPlaying.collectAsState()

                    Row(

                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(64.dp)) {

                            Icon(

                                imageVector = Icons.Rounded.FastRewind, 

                                contentDescription = "Previous",

                                tint = textColor,

                                modifier = Modifier.size(52.dp)

                            )

                        }

                        Box(

                            modifier = Modifier

                                .size(80.dp)

                                .clickable { viewModel.togglePlayPause() },

                            contentAlignment = Alignment.Center

                        ) {

                            com.aeswox.arcmusic.ui.components.PlayPauseMorphIcon(
                                isPlaying = isPlaying,
                                tint = textColor,
                                modifier = Modifier.size(50.dp)
                            )

                        }

                        IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(64.dp)) {

                            Icon(

                                imageVector = Icons.Rounded.FastForward, 

                                contentDescription = "Next",

                                tint = textColor,

                                modifier = Modifier.size(52.dp)

                            )

                        }

                    }

                    

                    Spacer(modifier = Modifier.height(64.dp))

                    

                    // Bottom Row

                    Row(

                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),

                        horizontalArrangement = Arrangement.SpaceBetween,

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        IconButton(onClick = { viewModel.toggleShuffleMode() }) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        

                        Row(

                            modifier = Modifier

                                .width(136.dp)

                                .height(44.dp)

                                .clip(RoundedCornerShape(22.dp))

                                .background(textColor.copy(alpha = 0.15f)),

                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            Box(

                                modifier = Modifier

                                    .weight(1f)

                                    .fillMaxHeight()

                                    .clickable { showDeviceSheet = true },

                                contentAlignment = Alignment.Center

                            ) {

                                Icon(

                                    imageVector = Icons.Outlined.Speaker,

                                    contentDescription = "Device",

                                    tint = textColor,

                                    modifier = Modifier.size(20.dp)

                                )

                            }

                            Box(

                                modifier = Modifier

                                    .width(1.dp)

                                    .height(20.dp)

                                    .background(textColor.copy(alpha = 0.3f))

                            )

                            Box(

                                modifier = Modifier

                                    .weight(1f)

                                    .fillMaxHeight()

                                    .clickable { showSleepTimerDialog = true },

                                contentAlignment = Alignment.Center

                            ) {

                                if (isTimerActive && sleepTimerTriggerTime != -1L) {

                                    Text(

                                        text = formatDuration(sleepTimerTimeLeft.coerceAtLeast(0)),

                                        style = MaterialTheme.typography.labelMedium,

                                        color = textColor

                                    )

                                } else {

                                    Icon(

                                        imageVector = Icons.Outlined.Timer,

                                        contentDescription = "Timer",

                                        tint = if (isTimerActive) MaterialTheme.colorScheme.primary else textColor,

                                        modifier = Modifier.size(20.dp)

                                    )

                                }

                            }

                        }



                        IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    androidx.media3.common.Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                    else -> Icons.Default.Repeat
                                },
                                contentDescription = "Repeat",
                                tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(32.dp))

                }

            }

        }



        // Full-screen lyrics overlay â€” placed at outer Box level to cover entire screen

        androidx.compose.animation.AnimatedVisibility(

            visible = showLyrics,

            enter = androidx.compose.animation.fadeIn(animationSpec = tween(400)),

            exit = androidx.compose.animation.fadeOut(animationSpec = tween(350)),

            modifier = Modifier.fillMaxSize()

        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                FullScreenWordSyncedLyrics(textColor = textColor)

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



@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FullScreenWordSyncedLyrics(textColor: Color = Color.White) {
    val viewModel: MusicViewModel = hiltViewModel()
    val currentlyPlayingEntity by viewModel.currentlyPlaying.collectAsState()
    val randomPicks by viewModel.randomPicks.collectAsState()
    val libraryTracks by viewModel.libraryTracks.collectAsState()
    
    val rawSongToPlay = currentlyPlayingEntity ?: randomPicks.firstOrNull()
    val songToPlay = libraryTracks.find { it.id == rawSongToPlay?.id } ?: rawSongToPlay

    val imageUrl = songToPlay?.albumId?.let { "content://media/external/audio/albumart/$it" } ?: ""

    val lyricsData by viewModel.lyricsUiState.collectAsState()

    val lyricsDisplayStyle by viewModel.lyricsDisplayStyle.collectAsState()
    val lyricsShowControls by viewModel.lyricsShowControls.collectAsState()
    val lyricsFadeSteepness by viewModel.lyricsFadeSteepness.collectAsState()
    val lyricsFadeScaleCeiling by viewModel.lyricsFadeScaleCeiling.collectAsState()
    val lyricsFadeDistanceSizing by viewModel.lyricsFadeDistanceSizing.collectAsState()
    val lyricsBlurRadius by viewModel.lyricsBlurRadius.collectAsState()
    val lyricsBlurDimming by viewModel.lyricsBlurDimming.collectAsState()

    val rawSyncedLines = lyricsData?.synced
    val plainLines = lyricsData?.plain

    val duration by viewModel.duration.collectAsState()

    val syncedLines = remember(rawSyncedLines, duration) {
        if (rawSyncedLines.isNullOrEmpty()) return@remember null
        
        val enriched = mutableListOf<com.aeswox.arcmusic.data.model.SyncedLine>()
        val gapThreshold = 10000 // 10 seconds
        
        if (rawSyncedLines.first().time > gapThreshold) {
            enriched.add(com.aeswox.arcmusic.data.model.SyncedLine(time = 2000, line = "● ● ●"))
        }
        
        for (i in 0 until rawSyncedLines.size - 1) {
            enriched.add(rawSyncedLines[i])
            val currentLineTime = rawSyncedLines[i].time
            val nextLineTime = rawSyncedLines[i+1].time
            if (nextLineTime - currentLineTime > gapThreshold) {
                enriched.add(com.aeswox.arcmusic.data.model.SyncedLine(time = currentLineTime + 5000, line = "● ● ●"))
            }
        }
        
        if (rawSyncedLines.isNotEmpty()) {
            enriched.add(rawSyncedLines.last())
            val lastTime = rawSyncedLines.last().time
            if (duration > 0 && (duration - lastTime) > gapThreshold) {
                enriched.add(com.aeswox.arcmusic.data.model.SyncedLine(time = lastTime + 5000, line = "● ● ●"))
            }
        }
        
        enriched.toList()
    }

    val currentPositionState = viewModel.currentPlaybackPosition.collectAsState()

    val linesToRender = remember(syncedLines, plainLines) {
        syncedLines?.map { it.line } ?: plainLines ?: listOf("No lyrics available")
    }

    

    var activeLineIndex by remember { mutableIntStateOf(0) }

    var activeWordIndex by remember { mutableIntStateOf(0) }



    LaunchedEffect(syncedLines) {

        viewModel.currentPlaybackPosition.collect { pos ->

            if (!syncedLines.isNullOrEmpty()) {

                val lastMatchIndex = syncedLines.indexOfLast { it.time <= pos }
                val newLineIndex = if (lastMatchIndex >= 0) {
                    val matchTime = syncedLines[lastMatchIndex].time
                    // If multiple lines have the exact same timestamp, pick the first one so we don't skip lines
                    syncedLines.indexOfFirst { it.time == matchTime }
                } else {
                    0
                }

                if (activeLineIndex != newLineIndex) {
                    activeLineIndex = newLineIndex
                }

                

                if (newLineIndex >= 0 && newLineIndex < syncedLines.size) {

                    val line = syncedLines[newLineIndex]

                    if (!line.words.isNullOrEmpty()) {

                        val newWordIndex = line.words.indexOfLast { it.time <= pos }.coerceAtLeast(0)

                        if (activeWordIndex != newWordIndex) {

                            activeWordIndex = newWordIndex

                        }

                    } else {

                        if (activeWordIndex != -1) {

                            activeWordIndex = -1

                        }

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



    val listState = rememberLazyListState()



    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex >= 0 && activeLineIndex < linesToRender.size) {
            val visibleItem = listState.layoutInfo.visibleItemsInfo.find { it.index == activeLineIndex }
            if (visibleItem != null && visibleItem.offset != 0) {
                listState.animateScrollBy(
                    value = visibleItem.offset.toFloat(),
                    animationSpec = androidx.compose.animation.core.spring<Float>(
                        dampingRatio = 0.75f, // slight bounce
                        stiffness = 50f // smooth and slow
                    )
                )
            } else {
                listState.animateScrollToItem(activeLineIndex)
            }
        }
    }



    var accentColor by remember { mutableStateOf(Color(0xFFB28D84)) }
    var isWhiteArtwork by remember { mutableStateOf(false) } // true when artwork bottom is near-white
    val isDarkTheme = isSystemInDarkTheme()
    val isArtworkDark by remember(accentColor) { derivedStateOf { accentColor.luminance() < 0.4f } }
    val lightThemeBgColor = if (isArtworkDark) accentColor else androidx.compose.ui.graphics.lerp(accentColor, Color.White, 0.7f)

    Box(modifier = Modifier.fillMaxSize()) {

        val density = androidx.compose.ui.platform.LocalDensity.current
        val imageRequest = ImageRequest.Builder(LocalContext.current)

            .data(imageUrl)

            .allowHardware(false)

            .build()



        AsyncImage(

            model = imageRequest,

            contentDescription = null,

            contentScale = ContentScale.Crop,

            onSuccess = { state ->

                val drawable = state.result.drawable

                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap

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

                .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }.blur(80.dp)

        )

        

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme) Color.Black.copy(alpha = 0.5f) else lightThemeBgColor.copy(alpha = 0.5f))
        )

        

        Box(

            modifier = Modifier

                .fillMaxWidth()

                .fillMaxHeight(0.7f)

                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = 0.0f), 
                            (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = 0.4f), 
                            (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = 0.8f)
                        ),
                        startY = 0f
                    )
                )
        )

        val listSpacing = if (lyricsDisplayStyle == LyricsDisplayStyle.FADE) 42.dp else 28.dp
        
        val bottomPadding = if (lyricsShowControls) 320.dp else 160.dp

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 160.dp, bottom = bottomPadding, start = 28.dp, end = 28.dp),
            verticalArrangement = Arrangement.spacedBy(listSpacing)
        ) {
            itemsIndexed(linesToRender) { lineIndex, line ->
                val words = remember(lineIndex, syncedLines, line) {
                    if (!syncedLines.isNullOrEmpty() && !syncedLines[lineIndex].words.isNullOrEmpty()) {
                        syncedLines[lineIndex].words!!.map { it.word }
                    } else {
                        line.split(" ")
                    }
                }

                if (lyricsDisplayStyle == LyricsDisplayStyle.FADE) {
                    FadeLyricLine(
                        lineIndex = lineIndex,
                        syncedLine = syncedLines?.getOrNull(lineIndex),
                        plainWords = words,
                        activeLineIndexProvider = activeLineIndexProvider,
                        currentPositionProvider = { currentPositionState.value },
                        listState = listState,
                        textColor = textColor,
                        fadeSteepness = lyricsFadeSteepness,
                        fadeScaleCeiling = lyricsFadeScaleCeiling,
                        distanceSizing = lyricsFadeDistanceSizing
                    )
                } else {
                    LyricLine(
                        line = line,
                        words = words,
                        lineIndex = lineIndex,
                        activeLineIndexProvider = activeLineIndexProvider,
                        activeWordIndexProvider = activeWordIndexProvider,
                        textColor = textColor,
                        blurRadiusMax = lyricsBlurRadius,
                        blurDimming = lyricsBlurDimming
                    )
                }
            }
        }
        
        // Persistent bottom controls for lyrics screen
        androidx.compose.animation.AnimatedVisibility(
            visible = lyricsShowControls,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = 0.6f),
                                (if (isDarkTheme) Color.Black else lightThemeBgColor).copy(alpha = 0.95f),
                                (if (isDarkTheme) Color.Black else lightThemeBgColor)
                            )
                        )
                    )
                .padding(top = 64.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title and Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = songToPlay?.title ?: "Unknown",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = textColor,
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
                            text = songToPlay?.artist ?: "Unknown",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        JellyIconButton(
                            onClick = {
                                songToPlay?.let { track ->
                                    viewModel.toggleFavorite(listOf(track.id), !track.isFavorite)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (songToPlay?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (songToPlay?.isFavorite == true) Color.White else textColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                ScrubberAndTimer(viewModel = viewModel, textColor = textColor, textAlpha = 0.7f, songToPlay = songToPlay, showBadges = false)
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Controls
                val isPlaying by viewModel.isPlaying.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(64.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.FastRewind, 
                            contentDescription = "Previous",
                            tint = textColor,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        com.aeswox.arcmusic.ui.components.PlayPauseMorphIcon(
                            isPlaying = isPlaying,
                            tint = textColor,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(64.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward, 
                            contentDescription = "Next",
                            tint = textColor,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }
        }
        } // End of AnimatedVisibility
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
