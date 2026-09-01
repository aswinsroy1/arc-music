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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import coil.compose.AsyncImage

import coil.request.ImageRequest

import androidx.compose.ui.platform.LocalContext

import androidx.palette.graphics.Palette

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
    
    val isLight = accentColor.luminance() > 0.6f
    
    val scrimHeightFraction by animateFloatAsState(targetValue = if (showLyrics) 1f else 0.7f, label = "height")
    val scrimStartAlpha by animateFloatAsState(targetValue = if (showLyrics) 0.0f else 0f, label = "startAlpha")
    val midAlphaRatio by animateFloatAsState(targetValue = if (showLyrics) 0.0f else if (isLight) 0.5f else 0.1f, label = "midAlphaRatio")
    val endAlphaRatio by animateFloatAsState(targetValue = if (showLyrics) 0.1f else if (isLight) 0.9f else 0.3f, label = "endAlphaRatio")
    val baseScrimAlpha by animateFloatAsState(targetValue = if (showLyrics) 0.15f else if (isLight) 0.3f else 0.05f, label = "baseScrim")
    val sharpImageAlpha by animateFloatAsState(targetValue = if (showLyrics) 0f else 1f, label = "sharpImageAlpha")
    val controlsAlpha by animateFloatAsState(targetValue = if (showLyrics) 0f else 1f, label = "controlsAlpha")
    
    val textColor = Color.White
    val textAlpha = 0.7f

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

                        Palette.from(bitmap).generate { palette ->

                            palette?.dominantSwatch?.rgb?.let { color ->

                                accentColor = Color(color)

                            } ?: palette?.mutedSwatch?.rgb?.let { color ->

                                accentColor = Color(color)

                            }

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

                    .background(Color.Black.copy(alpha = baseScrimAlpha))

            )



            // Sharp image in the top half, full bleed without rounded corners
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { showLyrics = true }
                    .graphicsLayer {
                        alpha = sharpImageAlpha
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        // Fade out the bottom 30% of the sharp artwork so it smoothly blends into the blurred background
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = size.height * 0.7f,
                                endY = size.height
                            ),
                            blendMode = BlendMode.DstOut
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

                                Color.Black.copy(alpha = scrimStartAlpha), 

                                Color.Black.copy(alpha = midAlphaRatio), 

                                Color.Black.copy(alpha = endAlphaRatio)

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

                            Text(
                                text = songToPlay?.title ?: "Unknown",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = textColor,
                                maxLines = 1
                            )
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

                    androidx.palette.graphics.Palette.from(bitmap).generate { palette ->

                        palette?.dominantSwatch?.rgb?.let { color ->

                            accentColor = Color(color)

                        } ?: palette?.mutedSwatch?.rgb?.let { color ->

                            accentColor = Color(color)

                        }

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
                        Text(
                            text = songToPlay?.title ?: "Unknown",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = textColor,
                            maxLines = 1
                        )
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
@Composable
fun ScrubberAndTimer(
    viewModel: MusicViewModel,
    textColor: Color,
    textAlpha: Float,
    songToPlay: Track?,
    showBadges: Boolean = true
) {
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    
    val progress = if (isSeeking) {
        sliderPosition
    } else {
        if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(textColor.copy(alpha = 0.25f))
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
                    onDragCancel = {
                        isSeeking = false
                    }
                ) { change, dragAmount ->
                    change.consume()
                    sliderPosition = (sliderPosition + dragAmount / size.width).coerceIn(0f, 1f)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5.dp))
                .background(textColor)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    
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




