package com.aeswox.arcmusic

import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Check

import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext

import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import com.aeswox.arcmusic.backdrop.backdrops.layerBackdrop

import androidx.compose.ui.draw.blur
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.ui.theme.ArcMusicTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.runtime.remember

import androidx.compose.animation.core.*

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels

@AndroidEntryPoint
@kotlin.OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {
    private val activityViewModel: MusicViewModel by viewModels()
    
    private var keepSplashScreen = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val viewModel: MusicViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isLibraryLoaded by viewModel.isLibraryLoaded.collectAsState()
            
            androidx.compose.runtime.LaunchedEffect(isLibraryLoaded) {
                if (isLibraryLoaded) {
                    // Small delay to ensure the UI is fully drawn before the splash screen hides
                    kotlinx.coroutines.delay(100)
                    keepSplashScreen = false
                }
            }
            
            val isDarkTheme = when (themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            ArcMusicTheme(darkTheme = isDarkTheme) {
                val baseBg = MaterialTheme.colorScheme.background
                val appBackdrop = com.aeswox.arcmusic.backdrop.backdrops.rememberLayerBackdrop {
                    drawRect(baseBg)
                    drawContent()
                }

                androidx.compose.runtime.CompositionLocalProvider(
                    LocalAppBackdrop provides appBackdrop
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        val navController = rememberNavController()
                    val tintTransparency by viewModel.tintTransparency.collectAsState()
                    val noiseFactor by viewModel.noiseFactor.collectAsState()
                    val glowIntensity by viewModel.glowIntensity.collectAsState()
                    val lightThemeForNowPlaying by viewModel.lightThemeForNowPlaying.collectAsState()
                    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
                    val isMiniPlayerVisible by viewModel.isMiniPlayerVisible.collectAsState()
                    val artworkUrl = if (isMiniPlayerVisible) currentlyPlaying?.albumId?.let { "content://media/external/audio/albumart/$it" } else null
                    val glowColor by rememberDominantColor(imageUrl = artworkUrl, defaultColor = Color(0xFF5E90A7))

                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedGlowBackground(glowIntensity = glowIntensity, color = glowColor)
                        
                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                        composable("home") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                MusicHomeScreen(
                                    onNavigateToCollectionGrowth = { navController.navigate("collection_growth") },
                                    onNavigateToCollectionHealth = { navController.navigate("collection_health") },
                                    tintTransparency = tintTransparency,
                                    noiseFactor = noiseFactor,
                                    glowIntensity = glowIntensity,
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToNowPlaying = { navController.navigate("now_playing") },
                                    onNavigateToAlbumDetails = { albumId -> navController.navigate("album_details/$albumId") },
                                    onNavigateToPlaylistDetails = { playlistId -> navController.navigate("playlist_details/$playlistId") },
                                    onNavigateToArtistDetails = { artistId -> navController.navigate("artist_details/$artistId") },
                                    viewModel = viewModel
                                )
                            }
                        }
                                                composable("collection_growth") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                CollectionGrowthScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    glowIntensity = glowIntensity,
                                    viewModel = viewModel
                                )
                            }
                        }
                        composable("collection_health") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                CollectionHealthScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToMissingContent = { navController.navigate("missing_content") },
                                    onNavigateToMissingArtwork = { navController.navigate("missing_artwork") },
                                    glowIntensity = glowIntensity
                                )
                            }
                        }
                        composable("missing_content") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                MissingContentScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = viewModel
                                )
                            }
                        }
                        composable("missing_artwork") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                MissingArtworkScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("artist_details/{artistId}") { backStackEntry ->
                            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
                            Box(modifier = Modifier.padding(innerPadding)) {
                                ArtistDetailsScreen(
                                    artistId = artistId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAlbum = { albumId -> navController.navigate("album_details/$albumId") },
                                    onNavigateToAllTracks = { aId -> navController.navigate("artist_tracks/$aId") },
                                    onNavigateToAllAlbums = { aId -> navController.navigate("artist_albums/$aId") }
                                )
                            }
                        }
                        composable("artist_tracks/{artistId}") { backStackEntry ->
                            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
                            Box(modifier = Modifier.padding(innerPadding)) {
                                ArtistTracksScreen(
                                    artistId = artistId,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("artist_albums/{artistId}") { backStackEntry ->
                            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
                            Box(modifier = Modifier.padding(innerPadding)) {
                                ArtistAlbumsScreen(
                                    artistId = artistId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAlbum = { albumId -> navController.navigate("album_details/$albumId") }
                                )
                            }
                        }
                        composable("album_details/{albumId}") { backStackEntry ->
                            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
                            Box(modifier = Modifier.padding(innerPadding)) {
                                AlbumDetailsScreen(
                                    albumId = albumId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToArtist = { aId -> navController.navigate("artist_details/$aId") },
                                    onNavigateToAlbum = { aId -> navController.navigate("album_details/$aId") }
                                )
                            }
                        }
                        composable("playlist_details/{playlistId}") { backStackEntry ->
                            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
                            Box(modifier = Modifier.padding(innerPadding)) {
                                PlaylistDetailsScreen(
                                    playlistId = playlistId,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("now_playing") {
                            NowPlayingScreen(
                                tintTransparency = tintTransparency,
                                noiseFactor = noiseFactor,
                                glowIntensity = glowIntensity,
                                isDarkTheme = !lightThemeForNowPlaying,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToQueue = { navController.navigate("queue") },
                                onNavigateToAlbum = { albumTitle -> navController.navigate("album_details/$albumTitle") },
                                onNavigateToArtist = { artistName -> navController.navigate("artist_details/$artistName") }
                            )
                        }
                        composable("queue") {
                            QueueScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("settings") {
                            val context = LocalContext.current
                            val settingsPermissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                listOf(Manifest.permission.READ_MEDIA_AUDIO)
                            } else {
                                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            val settingsPermissionsState = rememberMultiplePermissionsState(permissions = settingsPermissionsList)
                            val lastFmApiKey by viewModel.lastFmApiKey.collectAsState()
                            val fanartTvApiKey by viewModel.fanartTvApiKey.collectAsState()
                            
                            Box(modifier = Modifier.padding(innerPadding)) {
                                SettingsScreen(
                                    tintTransparency = tintTransparency,
                                    noiseFactor = noiseFactor,
                                    glowIntensity = glowIntensity,
                                    themeMode = themeMode,
                                    lightThemeForNowPlaying = lightThemeForNowPlaying,
                                    lastFmApiKey = lastFmApiKey,
                                    fanartTvApiKey = fanartTvApiKey,
                                    onThemeModeChange = { viewModel.setThemeMode(it) },
                                    onLightThemeForNowPlayingChange = { viewModel.setLightThemeForNowPlaying(it) },
                                    onLastFmApiKeyChange = { viewModel.setLastFmApiKey(it) },
                                    onFanartTvApiKeyChange = { viewModel.setFanartTvApiKey(it) },
                                    onNavigateToAppearance = { navController.navigate("appearance") },
                                    onNavigateToEqualizer = { navController.navigate("equalizer") },
                                    onNavigateBack = { navController.popBackStack() },
                                    onScanMediaStore = {
                                        if (settingsPermissionsState.allPermissionsGranted) {
                                            viewModel.scanMediaStore()
                                        } else {
                                            settingsPermissionsState.launchMultiplePermissionRequest()
                                        }
                                    },
                                    onTestEac3 = { viewModel.testEac3Playback(context) }
                                )
                            }
                        }
                        composable("appearance") {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                AppearanceScreen(
                                    tintTransparency = tintTransparency,
                                    noiseFactor = noiseFactor,
                                    glowIntensity = glowIntensity,
                                    onTintTransparencyChange = { viewModel.setTintTransparency(it) },
                                    onNoiseFactorChange = { viewModel.setNoiseFactor(it) },
                                    onGlowIntensityChange = { viewModel.setGlowIntensity(it) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("equalizer") {
                            EqualizerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                    }
                    }
                    }
                }
            }
        }
    }
}

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun MusicHomeScreen(
    onNavigateToCollectionGrowth: () -> Unit = {},
    onNavigateToCollectionHealth: () -> Unit = {},
    onNavigateToPlaylistDetails: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    tintTransparency: Float = 0.4f,
    noiseFactor: Float = 0.06f,
    glowIntensity: Float = 0.6f,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToNowPlaying: () -> Unit = {},
    onNavigateToAlbumDetails: (String) -> Unit = {},
    onNavigateToArtistDetails: (String) -> Unit = {},
    viewModel: MusicViewModel = hiltViewModel()
) {
    val hazeState = remember { HazeState() }
    var bottomPadding by remember { mutableStateOf(240.dp) }
    var currentTab by remember { mutableIntStateOf(0) }
    var isLibrarySelectionMode by remember { mutableStateOf(false) }
    var showCreatePlaylistFlow by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current
    val currentlyPlayingEntity by viewModel.currentlyPlaying.collectAsState()
    val currentSong = currentlyPlayingEntity
    val artworkUrl = currentSong?.albumId?.let { "content://media/external/audio/albumart/$it" }
    val glowColor by rememberDominantColor(imageUrl = artworkUrl, defaultColor = Color(0xFF5E90A7))
    val libraryTracks by viewModel.libraryTracks.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isLibraryLoaded by viewModel.isLibraryLoaded.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMiniPlayerVisible by viewModel.isMiniPlayerVisible.collectAsState()

    LaunchedEffect(currentlyPlayingEntity, isPlaying) {
        if (currentlyPlayingEntity != null && isPlaying) {
            viewModel.setMiniPlayerVisible(true)
        }
    }

    val permissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsList)
    
    val onSongClick: (Track, List<Track>?) -> Unit = { song, queue ->
        viewModel.setCurrentlyPlaying(song, queue)
        viewModel.setMiniPlayerVisible(true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .applyHazeAndBackdrop(hazeState = hazeState)
        ) {
            when (currentTab) {
                0 -> {
                    if (isLibraryLoaded) {
                        if (libraryTracks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 24.dp, bottom = bottomPadding)
                        ) {
                            Header(modifier = Modifier.padding(horizontal = 24.dp), onSettingsClick = onNavigateToSettings)
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(32.dp).fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LibraryMusic,
                                            contentDescription = "No music",
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No music found",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Scan your device to find songs or grant storage permissions if you haven't yet.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(32.dp))
                                        if (isScanning) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        } else {
                                            AppPrimaryButton(
                                                text = "Scan Storage",
                                                onClick = {
                                                    if (!permissionsState.allPermissionsGranted) {
                                                        permissionsState.launchMultiplePermissionRequest()
                                                    } else {
                                                        viewModel.scanMediaStore()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding),
                            verticalArrangement = Arrangement.spacedBy(32.dp),
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            item {
                                Header(modifier = Modifier.padding(horizontal = 24.dp), onSettingsClick = onNavigateToSettings)
                            }
                            // Debug buttons moved to Settings > Developer section
    
                            item {
                                HeroSection(
                                    currentSong = currentSong,
                                    isPlaying = isMiniPlayerVisible,
                                    onPlayClick = onSongClick,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                            item {
                                RecentlyPlayedSection(onSongClick = onSongClick)
                            }
                            item {
                                RandomPicksSection(onSongClick = onSongClick)
                            }
                            item {
                                RecommendedDownloadsSection(onSongClick = onSongClick, onNavigateToCollectionGrowth = onNavigateToCollectionGrowth)
                            }
                            item {
                                CollectionHealthSection(onClick = onNavigateToCollectionHealth)
                            }
                            item {
                                val stats by viewModel.listeningStats.collectAsState()
                                ListeningStatsSection(stats = stats, onClick = { currentTab = 3 })
                            }
                        }
                    }
                }
                }
                1 -> {
                    if (selectedGenre != null) {
                        GenreHubScreenContent(
                            genreName = selectedGenre!!,
                            bottomPadding = bottomPadding,
                            onNavigateBack = { selectedGenre = null }
                        )
                    } else {
                        SearchScreenContent(viewModel = viewModel, bottomPadding = bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onGenreClick = { selectedGenre = it })
                    }
                }
                2 -> {
                    LibraryScreenContent(bottomPadding = if (isLibrarySelectionMode) 100.dp else bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onSelectionModeChange = { isLibrarySelectionMode = it }, onCreatePlaylistClick = { showCreatePlaylistFlow = true })
                }
                3 -> {
                    val stats by viewModel.listeningStats.collectAsState()
                    ListeningStatsScreenContent(stats = stats, bottomPadding = bottomPadding, onNavigateBack = { currentTab = 0 })
                }
            }
        }
        
        if (!isLibrarySelectionMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                    .onSizeChanged { size ->
                        bottomPadding = with(density) { size.height.toDp() } + 48.dp
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            if (isMiniPlayerVisible && currentSong != null) {
                MiniPlayer(
                    title = currentSong!!.title,
                    artist = currentSong!!.artist,
                    imageUrl = currentSong!!.albumId?.let { "content://media/external/audio/albumart/$it" } ?: "",
                    hazeState = hazeState, 
                    tintTransparency = tintTransparency, 
                    noiseFactor = noiseFactor, 
                    isPlaying = isPlaying,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onSkipNextClick = { viewModel.skipToNext() },
                    onClick = onNavigateToNowPlaying,
                    onDismiss = { 
                        viewModel.setMiniPlayerVisible(false)
                        viewModel.pause()
                    }
                )
            }
            BottomNavigation(
                currentTab = currentTab,
                onTabSelected = { 
                    currentTab = it
                    if (it != 1) selectedGenre = null
                },
                hazeState = hazeState, 
                tintTransparency = tintTransparency, 
                noiseFactor = noiseFactor
            )
        }
        
        if (showCreatePlaylistFlow) {
            val tracks by viewModel.libraryTracks.collectAsState()
            CreatePlaylistFlow(
                tracks = tracks,
                hazeState = hazeState,
                glowColor = glowColor,
                onDismiss = { showCreatePlaylistFlow = false },
                onCreatePlaylist = { name, description, coverArtUri, trackIds ->
                    viewModel.createPlaylist(name, description, coverArtUri, trackIds)
                }
            )
        }

        }
    }
}

@Composable
fun Header(modifier: Modifier = Modifier, title: String? = "Arc Music", onSettingsClick: () -> Unit = {}, onBackClick: () -> Unit = { /*TODO*/ }) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (title != null) {
            Text(
                text = title, 
                style = MaterialTheme.typography.displayLarge, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Down",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.MoreVert, 
                contentDescription = "More", 
                tint = MaterialTheme.colorScheme.onSurface, 
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun HeroSection(
    currentSong: Track?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayClick: (Track, List<Track>?) -> Unit = { _, _ -> }
) {
    val viewModel: MusicViewModel = hiltViewModel()
    val randomPicks by viewModel.randomPicks.collectAsState()

    if (isPlaying && currentSong != null) {
        var showLyrics by remember { mutableStateOf(false) }

        val blurRadius by animateDpAsState(if (showLyrics) 24.dp else 0.dp, label = "blurRadius")
        val gradientAlphaStart by animateFloatAsState(if (showLyrics) 0.6f else 0.0f, label = "gradStart")
        val gradientAlphaMid by animateFloatAsState(if (showLyrics) 0.6f else 0.2f, label = "gradMid")
        val gradientAlphaEnd by animateFloatAsState(if (showLyrics) 0.6f else 0.8f, label = "gradEnd")

        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1.57f)
                .clip(RoundedCornerShape(36.dp))
                .clickable { showLyrics = !showLyrics }
        ) {
            AsyncImage(
                model = currentSong.albumId?.let { "content://media/external/audio/albumart/$it" },
                contentDescription = currentSong.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(blurRadius)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = gradientAlphaStart), 
                                Color.Black.copy(alpha = gradientAlphaMid), 
                                Color.Black.copy(alpha = gradientAlphaEnd)
                            )
                        )
                    )
            )
            
            androidx.compose.animation.AnimatedVisibility(
                visible = showLyrics,
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)),
                exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                WordSyncedLyrics()
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !showLyrics,
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)),
                exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = currentSong.title, 
                        style = MaterialTheme.typography.headlineLarge, 
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong.artist, 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    } else {
        val suggestedSong = randomPicks.firstOrNull()
        if (suggestedSong == null) return
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1.57f)
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                val gradientBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF45B0E6), Color(0xFF4DE3C3))
                )
                Text(
                    text = suggestedSong?.title ?: "Unknown",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = gradientBrush,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 36.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = suggestedSong?.artist ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                AppPrimaryButton(
                    text = "Play",
                    onClick = { suggestedSong?.let { song -> onPlayClick(song, randomPicks) } },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp)
                )
            }
        }
    }
}


@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun WordSyncedLyrics(textColor: Color = Color.White) {
    val viewModel: MusicViewModel = hiltViewModel()
    val lyricsData by viewModel.lyricsUiState.collectAsState()

    val syncedLines = lyricsData?.synced
    val plainLines  = lyricsData?.plain
    val lines = remember(syncedLines, plainLines) {
        syncedLines?.map { it.line } ?: plainLines ?: listOf("♪")
    }

    var activeLineIndex by remember { mutableIntStateOf(0) }
    var activeWordIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(syncedLines) {
        viewModel.currentPlaybackPosition.collect { pos ->
            if (!syncedLines.isNullOrEmpty()) {
                val newLine = syncedLines.indexOfLast { it.time <= pos }.coerceAtLeast(0)
                if (activeLineIndex != newLine) activeLineIndex = newLine

                val line = syncedLines[newLine]
                if (!line.words.isNullOrEmpty()) {
                    val newWord = line.words.indexOfLast { it.time <= pos }.coerceAtLeast(0)
                    if (activeWordIndex != newWord) activeWordIndex = newWord
                } else {
                    activeWordIndex = -1
                }
            } else {
                activeLineIndex = 0
                activeWordIndex = -1
            }
        }
    }

    val activeLine = lines.getOrElse(activeLineIndex) { "♪" }
    val activeWords = remember(activeLineIndex, syncedLines, activeLine) {
        if (!syncedLines.isNullOrEmpty() && !syncedLines[activeLineIndex].words.isNullOrEmpty()) {
            syncedLines[activeLineIndex].words!!.map { it.word }
        } else {
            activeLine.split(" ")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = activeLineIndex,
            transitionSpec = {
                (androidx.compose.animation.fadeIn(tween(400)) +
                 androidx.compose.animation.slideInVertically(tween(400)) { it / 3 })
                    .togetherWith(
                        androidx.compose.animation.fadeOut(tween(250)) +
                        androidx.compose.animation.slideOutVertically(tween(250)) { -it / 3 }
                    )
            },
            label = "lyricLine"
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                activeWords.forEachIndexed { wordIndex, word ->
                    val isHighlighted = wordIndex == activeWordIndex
                    LyricWord(
                        word = word,
                        isHighlighted = isHighlighted,
                        isLineActive = true,
                        textColor = textColor,
                        baseFontSize = 22f
                    )
                }
            }
        }
    }
}


@Composable
fun HorizontalArtworkListSection(title: String, songs: List<Track>, onSongClick: (Track, List<Track>?) -> Unit = { _, _ -> }, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp), 
        modifier = modifier
    ) {
        Text(
            text = title, 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs) { song ->
                AsyncImage(
                    model = song.albumId?.let { "content://media/external/audio/albumart/$it" },
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onSongClick(song, songs) }
                )
            }
        }
    }
}

@Composable
fun RandomPicksSection(modifier: Modifier = Modifier, onSongClick: (Track, List<Track>?) -> Unit = { _, _ -> }) {
    val viewModel: MusicViewModel = hiltViewModel()
    val songs by viewModel.randomPicks.collectAsState()
    if (songs.isEmpty()) return
    
    HorizontalArtworkListSection(title = "Random picks", songs = songs, onSongClick = onSongClick, modifier = modifier)
}


@Composable
fun RecentlyPlayedSection(modifier: Modifier = Modifier, onSongClick: (Track, List<Track>?) -> Unit = { _, _ -> }) {
    val viewModel: MusicViewModel = hiltViewModel()
    val songs by viewModel.recentlyPlayed.collectAsState()
    
    if (songs.isEmpty()) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Recently Played", 
            style = MaterialTheme.typography.headlineMedium, 
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                songs.forEach { song ->
                    RecentlyPlayedItem(song, onSongClick = { s -> onSongClick(s, songs) })
                }
            }
        }
    }
}

@Composable
fun RecentlyPlayedItem(song: Track, onSongClick: (Track) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick(song) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.albumId?.let { "content://media/external/audio/albumart/$it" },
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title, 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.artist, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.MoreVert, 
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecommendedDownloadsSection(onSongClick: (Track, List<Track>?) -> Unit, onNavigateToCollectionGrowth: () -> Unit = {}, modifier: Modifier = Modifier) {
    val viewModel: MusicViewModel = hiltViewModel()
    val songs by viewModel.recommended.collectAsState()
    
    if (songs.isEmpty()) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToCollectionGrowth() }
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recommended Downloads", 
                style = MaterialTheme.typography.headlineMedium, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go to Collection Growth",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                songs.forEach { song ->
                    RecommendedDownloadItem(song, onSongClick = { s -> onSongClick(s, songs) })
                }
            }
        }
    }
}

@Composable
fun RecommendedDownloadItem(song: Track, onSongClick: (Track) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick(song) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.albumId?.let { "content://media/external/audio/albumart/$it" },
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title, 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.artist, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Outlined.Download, 
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)),
        content = content
    )
}

@Composable
fun ListeningStatsSection(
    stats: ListeningStatsData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Derive display values from real data
    val topArtist = stats.topArtists.firstOrNull()
    val topGenre  = stats.topGenres.firstOrNull()

    // Weekly total: sum of the last 7 days
    val weeklyMinutes = stats.weeklyMinutesByDay.sum()
    val weeklyHours   = weeklyMinutes / 60L
    val weeklyMins    = weeklyMinutes % 60L
    val weeklyText    = when {
        weeklyHours > 0 && weeklyMins > 0 -> "${weeklyHours}.${weeklyMins / 6} hrs"
        weeklyHours > 0                   -> "${weeklyHours} hrs"
        weeklyMinutes > 0                 -> "${weeklyMinutes} min"
        else                              -> "—"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Listening Stats",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        GlassCard(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clickable { onClick() }
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // ── Row 1: Top Artist | Favorite Genre ──────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Artist
                    Column {
                        Text(
                            text = "TOP ARTIST",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (topArtist != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (topArtist.photoUri != null) {
                                    AsyncImage(
                                        model = topArtist.photoUri,
                                        contentDescription = topArtist.artistName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = topArtist.artistName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Text(
                                text = "No data yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Favorite Genre
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "FAVORITE GENRE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (topGenre != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = topGenre.genre,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Row 2: Weekly Listening | Mini bar chart ─────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "WEEKLY LISTENING",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weeklyText,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Mini bar chart from real weekly data
                    val maxMinutes = stats.weeklyMinutesByDay.maxOrNull()?.takeIf { it > 0L } ?: 1L
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(40.dp)
                    ) {
                        stats.weeklyMinutesByDay.forEach { minutes ->
                            val heightFraction = (minutes.toFloat() / maxMinutes).coerceIn(0.04f, 1f)
                            val alpha = if (heightFraction >= 0.95f) 1f else heightFraction * 0.75f + 0.15f
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha.coerceIn(0.2f, 1f))
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(viewModel: MusicViewModel, modifier: Modifier = Modifier, bottomPadding: androidx.compose.ui.unit.Dp, onNavigateToAlbumDetails: (String) -> Unit = {}, onNavigateToPlaylistDetails: (String) -> Unit = {}, onNavigateToArtistDetails: (String) -> Unit = {}, onGenreClick: (String) -> Unit = {}) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val genreCounts by viewModel.genreCounts.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Reset selected filter when search query transitions between empty and active
    LaunchedEffect(searchQuery.isEmpty()) {
        selectedFilter = "All"
    }
    
    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            SearchHeader(modifier = Modifier.padding(horizontal = 24.dp))
        }
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { 
                    viewModel.saveRecentSearch(searchQuery)
                    keyboardController?.hide()
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        item {
            FilterChips(
                isSearchActive = searchQuery.isNotEmpty(),
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }
        
        if (searchQuery.isEmpty()) {
            if (selectedFilter == "All" || selectedFilter == "Recent") {
                if (recentSearches.isNotEmpty()) {
                    item { 
                        RecentSearchesSection(
                            recentSearches = recentSearches, 
                            onClearAll = { viewModel.clearAllRecentSearches() },
                            onClearItem = { viewModel.deleteRecentSearch(it) },
                            onItemClick = { 
                                viewModel.updateSearchQuery(it)
                                viewModel.saveRecentSearch(it)
                                keyboardController?.hide()
                            },
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) 
                    }
                }
            }
            if (selectedFilter == "All" || selectedFilter == "Genres") {
                item { BrowseCategoriesSection(genreCounts = genreCounts, modifier = Modifier.padding(horizontal = 24.dp), onGenreClick = onGenreClick) }
            }
        } else if (searchResults is SearchResultsUiState.Success) {
            val data = searchResults as SearchResultsUiState.Success
            
            val hasTopResult = data.tracks.firstOrNull() != null
            val hasSongs = data.tracks.isNotEmpty()
            val hasAlbums = data.albums.isNotEmpty()
            val hasArtists = data.artists.isNotEmpty()
            val hasPlaylists = data.playlists.isNotEmpty()

            val showTopResult = hasTopResult && (selectedFilter == "All" || selectedFilter == "Top result")
            val showSongs = hasSongs && (selectedFilter == "All" || selectedFilter == "Songs")
            val showAlbums = hasAlbums && (selectedFilter == "All" || selectedFilter == "Albums")
            val showArtists = hasArtists && (selectedFilter == "All" || selectedFilter == "Artists")
            val showPlaylists = hasPlaylists && (selectedFilter == "All" || selectedFilter == "Playlists")

            if (!showTopResult && !showSongs && !showAlbums && !showArtists && !showPlaylists) {
                item { SearchEmptyState(modifier = Modifier.padding(horizontal = 24.dp)) }
            } else {
                if (showTopResult) {
                    item { 
                        TopResultSection(
                            track = data.tracks.first(), 
                            modifier = Modifier.padding(horizontal = 24.dp),
                            onClick = { viewModel.setCurrentlyPlaying(data.tracks.first(), data.tracks) }
                        ) 
                    }
                }
                if (showSongs) {
                    val tracksToShow = if (selectedFilter == "All") data.tracks.take(4) else data.tracks
                    item { 
                        SongsResultSection(
                            tracks = tracksToShow, 
                            modifier = Modifier.padding(horizontal = 24.dp),
                            onTrackClick = { viewModel.setCurrentlyPlaying(it, data.tracks) }
                        ) 
                    }
                }
                if (showAlbums) {
                    item { AlbumsResultSection(albums = data.albums, modifier = Modifier.padding(horizontal = 24.dp), onNavigateToAlbumDetails = onNavigateToAlbumDetails) }
                }
                if (showArtists) {
                    item { ArtistsResultSection(artists = data.artists, modifier = Modifier.padding(horizontal = 24.dp), onNavigateToArtistDetails = onNavigateToArtistDetails) }
                }
                if (showPlaylists) {
                    item { PlaylistsResultSection(playlists = data.playlists, modifier = Modifier.padding(horizontal = 24.dp), onNavigateToPlaylistDetails = onNavigateToPlaylistDetails) }
                }
            }
        }
    }
}

@Composable
fun SearchHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Search", 
            style = MaterialTheme.typography.displayLarge, 
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreVert, 
                    contentDescription = "More", 
                    tint = MaterialTheme.colorScheme.onSurface, 
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search songs, albums, artists...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        )
        if (query.isEmpty()) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FilterChips(isSearchActive: Boolean = false, selectedFilter: String, onFilterSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    val filters = if (isSearchActive) {
        listOf("All", "Top result", "Songs", "Albums", "Artists", "Playlists")
    } else {
        listOf("All", "Songs", "Albums", "Artists", "Playlists", "Genres")
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun RecentSearchesSection(
    recentSearches: List<com.aeswox.arcmusic.db.entities.SearchHistory>,
    onClearAll: () -> Unit,
    onClearItem: (String) -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent searches",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Clear all",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onClearAll() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            recentSearches.forEach { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onClearItem(search.query) },
                                onTap = { onItemClick(search.query) }
                            )
                        }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = search.query,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class Category(val name: String, val rawName: String = name, val color: Color, val bgColor: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun BrowseCategoriesSection(genreCounts: List<MusicViewModel.GenreCount>, modifier: Modifier = Modifier, onGenreClick: (String) -> Unit = {}) {
    Column(modifier = modifier) {
        Text(
            text = "Browse categories",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        val fallbackColors = listOf(
            Color(0xFFFF6B6B),
            Color(0xFFFF8E3C),
            Color(0xFF9D4EDD),
            Color(0xFFFFB703),
            Color(0xFF34A853),
            Color(0xFFE91E63)
        )
        val fallbackIcons = listOf(Icons.Default.Star, Icons.Default.PlayArrow, Icons.Default.Language, Icons.Outlined.MusicNote, Icons.Default.Home, Icons.Default.Favorite)
        
        val categories = genreCounts.take(12).mapIndexed { index, genreCount ->
            val color = fallbackColors[index % fallbackColors.size]
            val icon = fallbackIcons[index % fallbackIcons.size]
            val nameWithCount = "${genreCount.genre.replaceFirstChar { it.uppercase() }} (${genreCount.count})"
            Category(nameWithCount, genreCount.genre, color, color, icon)
        }
        
        if (categories.isEmpty()) {
            Text(
                text = "No categories found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in categories.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CategoryCard(
                            category = categories[i],
                            modifier = Modifier.weight(1f),
                            onClick = { onGenreClick(categories[i].rawName) }
                        )
                        if (i + 1 < categories.size) {
                            CategoryCard(
                                category = categories[i + 1],
                                modifier = Modifier.weight(1f),
                                onClick = { onGenreClick(categories[i + 1].rawName) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .background(category.color.copy(alpha = 0.12f))
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = category.color
            )
        }
    }
}

@Composable
fun TopResultSection(track: com.aeswox.arcmusic.db.entities.Track, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(modifier = modifier) {
        Text(
            text = "Top result",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(36.dp))
                .clickable { onClick() }
        ) {
            val fallbackImage = "https://lh3.googleusercontent.com/aida/AP1WRLv2WecEYcFjvYBf-M3uEQ_any0wLnlOIbEuk_z6TQbKqKTSZoFVZNYQ-1t8glDuBotg9yeGnMK8FZEE-kgwiLAVRBXzvQimz6mY682dnzbndydZF2E-RtA81Z-B73vftEA1FvCkglrC0eRpulttSej5eBpotQsOJDlrWXWG2NcJDqKcgI2WBx09sqJfbw09cTRFbu54vdDLp3z42vq-SHV4IdKULZGUnC2hu9U6zJ1iQMIX1k012Bm-U6I"
            AsyncImage(
                model = track.artworkUri ?: fallbackImage,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SONG",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color.White
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SongsResultSection(tracks: List<com.aeswox.arcmusic.db.entities.Track>, modifier: Modifier = Modifier, onTrackClick: (com.aeswox.arcmusic.db.entities.Track) -> Unit = {}) {
    Column(modifier = modifier) {
        SectionHeader(title = "Songs")
        Spacer(modifier = Modifier.height(16.dp))
        GlassCard {
            Column(modifier = Modifier.padding(8.dp)) {
                val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuDssM_sY1Ws7j27eCFIebitvGUoY4mUDbFgTv7dfN1izfLLtj26xVAM7g3_BsPwJ1iWlo-A2KOxUK4unQDf5TsU8MU02QPyjLRsfDTNBORi16j91-T24IpynAAoNa67G9TZHqjAX_vYn_8nyp8IJ6Esgu3qsDlysGOOwvfF7akonsLsi9tqVmym_gOakEWdqT115gfsfVqe4_0XqZs5SyV2XEhyzJbZ7nurcuDH24b5Otnfibbyy2Nt1ZY4COqv_6q6x6H2zjxvI-Xa"
                tracks.forEach { track ->
                    val durationMs = track.durationMs
                    val minutes = durationMs / 1000 / 60
                    val seconds = (durationMs / 1000 % 60).toString().padStart(2, '0')
                    SongResultItem(
                        title = track.title, 
                        artist = track.artist, 
                        duration = "$minutes:$seconds", 
                        imageUrl = track.artworkUri ?: fallbackImage,
                        onClick = { onTrackClick(track) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun SongResultItem(title: String, artist: String, duration: String, imageUrl: String, isActive: Boolean = false, isSelectionMode: Boolean = false, isSelected: Boolean = false, isAtmos: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isActive) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.size(48.dp)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopStart)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                        .border(1.5.dp, if (isSelected) androidx.compose.ui.graphics.Color.Transparent else androidx.compose.ui.graphics.Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isAtmos) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_dolby_atmos),
                        contentDescription = "Dolby Atmos",
                        modifier = Modifier
                            .height(10.dp)
                            .padding(start = 8.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AlbumsResultSection(albums: List<com.aeswox.arcmusic.db.entities.Album>, modifier: Modifier = Modifier, onNavigateToAlbumDetails: (String) -> Unit = {}) {
    Column(modifier = modifier) {
        SectionHeader(title = "Albums")
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums) { album ->
                val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuCJx6j_t12Lixrr_kYaU8GRKRWceLe29sZI9YzJc0GLeyahwMHKbEC0ARLLWL-0RmYqBHjxwmZzgNqNyRZsCh8PjVOmyd_6FpltGB2ZT_2jSJjOT8ipdJzKfCUS1h3RWY2qsxJWOi3EOD8t0KBaOzpsuut79QihAF69rulfi88J3UM0uEC9UWv59NcUIbAA4flMoQKK67G82bGMK3o8oiFXpsKMS4SmVxG6s9JjrZ_ulmWltpRwT18rZusm2Ui_DldbXtXHUGkiFqXA"
                val yearStr = if (album.year != null && album.year > 0) "${album.year} • " else ""
                val subtitle = "$yearStr${album.trackCount} songs"
                AlbumResultItem(album.title, subtitle, album.artworkUri ?: fallbackImage, onClick = { onNavigateToAlbumDetails(album.title) })
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun AlbumResultItem(title: String, year: String, imageUrl: String, modifier: Modifier = Modifier.width(140.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                        .border(1.5.dp, if (isSelected) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = year,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ArtistsResultSection(artists: List<com.aeswox.arcmusic.db.entities.Artist>, modifier: Modifier = Modifier, onNavigateToArtistDetails: (String) -> Unit = {}) {
    Column(modifier = modifier) {
        SectionHeader(title = "Artists")
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(artists) { artist ->
                val fallbackImage = "https://lh3.googleusercontent.com/aida/AP1WRLv2WecEYcFjvYBf-M3uEQ_any0wLnlOIbEuk_z6TQbKqKTSZoFVZNYQ-1t8glDuBotg9yeGnMK8FZEE-kgwiLAVRBXzvQimz6mY682dnzbndydZF2E-RtA81Z-B73vftEA1FvCkglrC0eRpulttSej5eBpotQsOJDlrWXWG2NcJDqKcgI2WBx09sqJfbw09cTRFbu54vdDLp3z42vq-SHV4IdKULZGUnC2hu9U6zJ1iQMIX1k012Bm-U6I"
                ArtistResultItem(artist.name, artist.photoUri ?: fallbackImage, artist.isFavorite, onClick = { onNavigateToArtistDetails(artist.name) })
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun ArtistResultItem(name: String, imageUrl: String, isVerified: Boolean = false, modifier: Modifier = Modifier.width(100.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Box(modifier = Modifier.size(100.dp)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopStart)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f))
                        .border(1.5.dp, if (isSelected) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun PlaylistsResultSection(playlists: List<com.aeswox.arcmusic.db.entities.Playlist>, modifier: Modifier = Modifier, onNavigateToPlaylistDetails: (String) -> Unit = {}) {
    Column(modifier = modifier) {
        SectionHeader(title = "Playlists")
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(playlists) { playlist ->
                val fallbackImage = "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"
                PlaylistResultItem(title = playlist.name, subtitle = "Playlist", imageUrl = playlist.coverArtUri ?: fallbackImage, onClick = { onNavigateToPlaylistDetails(playlist.name) })
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun PlaylistResultItem(title: String, subtitle: String, imageUrl: String, modifier: Modifier = Modifier.width(280.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                    .border(1.5.dp, if (isSelected) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CollectionHealthSection(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Collection Health", 
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), 
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        GlassCard(
            modifier = Modifier.padding(horizontal = 24.dp).clickable { onClick() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COLLECTION HEALTH",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to Collection Health",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                        CircularProgressIndicator(
                            progress = { 0.84f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onSurface,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            strokeWidth = 3.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(
                            text = "84%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column {
                        Text(
                            text = "84%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your collection is in great\nshape.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Nothing found",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Try a different search term or check\nyour spelling.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "TRY SEARCHING FOR",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchSuggestionChip(label = "Artists")
            SearchSuggestionChip(label = "Playlists")
            SearchSuggestionChip(label = "Albums")
        }
    }
}

@Composable
fun SearchSuggestionChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
