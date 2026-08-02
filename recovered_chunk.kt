import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
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
fun NowPlayingScreen(
    tintTransparency: Float,
    noiseFactor: Float,
    glowIntensity: Float,
    isDarkTheme: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToQueue: () -> Unit = {},
    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {}
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
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }
    
    val deviceVolume by viewModel.deviceVolume.collectAsState()
    val deviceMaxVolume by viewModel.deviceMaxVolume.collectAsState()
    
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
    
    val scrimHeightFraction by animateFloatAsState(targetValue = if (showLyrics) 1f else 0.7f, label = "height")
    val scrimStartAlpha by animateFloatAsState(targetValue = if (showLyrics) 0.0f else 0f, label = "startAlpha")
    val midAlphaRatio by animateFloatAsState(targetValue = if (showLyrics) 0.0f else 0.4f, label = "midAlphaRatio")
    val endAlphaRatio by animateFloatAsState(targetValue = if (showLyrics) 0.1f else 0.8f, label = "endAlphaRatio")
    val baseScrimAlpha by animateFloatAsState(targetValue = if (showLyrics) 0.15f else 0.5f, label = "baseScrim")
    val sharpImageAlpha by animateFloatAsState(targetValue = if (showLyrics) 0f else 1f, label = "sharpImageAlpha")
    val controlsAlpha by animateFloatAsState(targetValue = if (showLyrics) 0f else 1f, label = "controlsAlpha")
    
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val textAlpha = if (isDarkTheme) 0.7f else 0.6f
        val imageUrl = songToPlay?.albumId?.let { "content://media/external/audio/albumart/$it" } ?: ""

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
                    .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }.blur(80.dp)
            )
            
            // Dark scrim over the blurred background to darken it
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkTheme) Color.Black.copy(alpha = baseScrimAlpha) else accentColor.copy(alpha = 0.3f))
            )

            // Sharp image in the top half, fading out at the bottom
            AsyncImage(
                model = imageUrl,
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
                    .align(Alignment.TopCenter)
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
            )
            
            // Extra gradient scrim at the bottom to ensure text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(scrimHeightFraction)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                (if (isDarkTheme) Color.Black else accentColor).copy(alpha = scrimStartAlpha), 
                                (if (isDarkTheme) Color.Black else accentColor).copy(alpha = midAlphaRatio), 
                                (if (isDarkTheme) Color.Black else accentColor).copy(alpha = endAlphaRatio)
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
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (showLyrics) showLyrics = false else onNavigateBack()
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown, 
                        contentDescription = "Collapse",
                        tint = textColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
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
                            if (isPlaying) {
                                CustomPauseIcon(color = textColor, modifier = Modifier.size(32.dp, 40.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow, 
                                    contentDescription = "Play",
                                    tint = textColor,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
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
                        IconButton(onClick = onNavigateToQueue) {
                            CustomListIcon(color = textColor, modifier = Modifier.size(24.dp))
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

                        IconButton(onClick = { showLyrics = !showLyrics }) {
                            CustomLyricsIcon(color = textColor, modifier = Modifier.size(24.dp))
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
                            .clickable { 
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
                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "audio/*"
                                            putExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri.parse(track.filePath))
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share song"))
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FullScreenWordSyncedLyrics(textColor: Color = Color.White) {
    val viewModel: MusicViewModel = hiltViewModel()
    val songToPlay by viewModel.currentlyPlaying.collectAsState()
    val imageUrl = songToPlay?.albumId?.let { "content://media/external/audio/albumart/$it" } ?: ""
    val lyricsData by viewModel.lyricsUiState.collectAsState()

    val syncedLines = lyricsData?.synced
    val plainLines = lyricsData?.plain

    val linesToRender = remember(syncedLines, plainLines) {
        syncedLines?.map { it.line } ?: plainLines ?: listOf("No lyrics available")
    }
    
    var activeLineIndex by remember { mutableIntStateOf(0) }
    var activeWordIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(syncedLines) {
        viewModel.currentPlaybackPosition.collect { pos ->
            if (!syncedLines.isNullOrEmpty()) {
                val newLineIndex = syncedLines.indexOfLast { it.time <= pos }.coerceAtLeast(0)
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
        if (activeLineIndex > 1) {
            listState.animateScrollToItem((activeLineIndex - 1).coerceAtLeast(0))
        }
    }

    var accentColor by remember { mutableStateOf(Color(0xFFB28D84)) }
    val isDarkTheme = isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
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
                .background(if (isDarkTheme) Color.Black.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.3f))
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
