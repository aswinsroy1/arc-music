package com.aeswox.arcmusic

import com.aeswox.arcmusic.sharing.ReceiveScreen
import com.aeswox.arcmusic.sharing.ShareScreen
import com.aeswox.arcmusic.db.entities.getQualityBadgeResId
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.ui.animations.NavTransitions
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.sharing.ShareScreen

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.AnimatedVisibilityScope

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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.db.entities.Track
import com.aeswox.arcmusic.ui.animations.JigglePhysicsSettings
import com.aeswox.arcmusic.ui.animations.LocalJigglePhysicsSettings
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.JellyIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledIconButton
import com.aeswox.arcmusic.ui.components.JellyFilledTonalIconButton
import com.aeswox.arcmusic.ui.components.JellyOutlinedIconButton

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

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
            val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
            
            androidx.compose.runtime.LaunchedEffect(isLibraryLoaded, hasCompletedOnboarding) {
                if (hasCompletedOnboarding != null && (isLibraryLoaded || hasCompletedOnboarding == false)) {
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

                val physicsMass by viewModel.physicsMass.collectAsState()
                val physicsStiffness by viewModel.physicsStiffness.collectAsState()
                val physicsDampingRatio by viewModel.physicsDampingRatio.collectAsState()
                val physicsAmplitude by viewModel.physicsAmplitude.collectAsState()
                val physicsGravity by viewModel.physicsGravity.collectAsState()

                androidx.compose.runtime.CompositionLocalProvider(
                    LocalAppBackdrop provides appBackdrop,
                    LocalJigglePhysicsSettings provides JigglePhysicsSettings(
                        mass = physicsMass,
                        stiffness = physicsStiffness,
                        dampingRatio = physicsDampingRatio,
                        amplitudeMultiplier = physicsAmplitude,
                        gravity = physicsGravity
                    )
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        val navController = rememberNavController()
                        val density = LocalDensity.current
                    val tintTransparency by viewModel.tintTransparency.collectAsState()
                    val noiseFactor by viewModel.noiseFactor.collectAsState()
                    val glowIntensity by viewModel.glowIntensity.collectAsState()
                    val lightThemeForNowPlaying by viewModel.lightThemeForNowPlaying.collectAsState()
                    val currentlyPlaying by viewModel.currentlyPlaying.collectAsState()
                    val isMiniPlayerVisible by viewModel.isMiniPlayerVisible.collectAsState()
                    val artworkUrl = if (isMiniPlayerVisible) currentlyPlaying?.artworkUri ?: currentlyPlaying?.albumId?.let { "content://media/external/audio/albumart/$it" } else null
                    val glowColor by rememberDominantColor(imageUrl = artworkUrl, defaultColor = Color(0xFF5E90A7))
                    
                    val view = androidx.compose.ui.platform.LocalView.current
                    if (!view.isInEditMode) {
                        val window = this@MainActivity.window
                        val baseBgLuminance = MaterialTheme.colorScheme.background.luminance()
                        val glowLuminance = glowColor.luminance()
                        val effectiveLuminance = glowLuminance * glowIntensity + baseBgLuminance * (1f - glowIntensity)
                        val isLightBg = effectiveLuminance > 0.5f
                        
                        androidx.compose.runtime.SideEffect {
                            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightBg
                        }
                    }

                    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState()
                    if (hasCompletedOnboarding == null) {
                        return@Scaffold
                    }
                    val startDest = remember(hasCompletedOnboarding) {
                        if (hasCompletedOnboarding == true) "home" else "onboarding"
                    }

                    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
                    val isPlaying by viewModel.isPlaying.collectAsState()
                    val hazeState = remember { HazeState() }
                    val globalNavBarHeight by viewModel.navBarHeight.collectAsState()
                    val globalNavBarVisible by viewModel.isNavBarVisible.collectAsState()
                    val isDarkThemeForNowPlaying = !lightThemeForNowPlaying
                    
                    var currentTab by rememberSaveable { mutableIntStateOf(0) }
                    var isLibrarySelectionMode by rememberSaveable { mutableStateOf(false) }
                    var showCreatePlaylistFlow by rememberSaveable { mutableStateOf(false) }
                    var selectedGenre by rememberSaveable { mutableStateOf<String?>(null) }
                    
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route ?: startDest
                    val isNavBarVisible = currentRoute == "home" && !isLibrarySelectionMode && currentTab in 0..2 && selectedGenre == null
                    
                    LaunchedEffect(isNavBarVisible) {
                        viewModel.setNavBarVisible(isNavBarVisible)
                        if (!isNavBarVisible) {
                            viewModel.setNavBarHeight(0.dp)
                        }
                    }
                    
                    // Raw (target) offset â€” driven by nav-bar visibility and library selection mode
                    val rawBottomOffset = if (isNavBarVisible || isLibrarySelectionMode) {
                        90.dp + innerPadding.calculateBottomPadding()
                    } else {
                        24.dp + innerPadding.calculateBottomPadding()
                    }

                    // Smooth spring transition so the miniplayer glides when the nav bar
                    // appears / disappears instead of jumping instantly.
                    val bottomOffset by animateDpAsState(
                        targetValue = rawBottomOffset,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "miniPlayerBottomOffset"
                    )

                    // Animate the mini-player's height contribution to content padding so
                    // lists don't jump when the player appears / disappears.
                    val rawMiniPlayerHeightContrib = if (isMiniPlayerVisible && currentlyPlaying != null && currentRoute != "onboarding") 80.dp else 0.dp
                    val animMiniPlayerHeightContrib by animateDpAsState(
                        targetValue = rawMiniPlayerHeightContrib,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "miniPlayerHeightContrib"
                    )
                    val contentBottomPadding = bottomOffset + animMiniPlayerHeightContrib

                    @OptIn(ExperimentalSharedTransitionApi::class)
                    SharedTransitionLayout {
                        androidx.compose.runtime.CompositionLocalProvider(
                            LocalSharedTransitionScope provides this
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                
                                com.aeswox.arcmusic.ui.components.PlayerBottomSheet(
                                    isExpanded = isPlayerExpanded,
                                    isVisible = isMiniPlayerVisible && currentlyPlaying != null && currentRoute != "onboarding",
                                    onExpand = { viewModel.setPlayerExpanded(true) },
                                    onCollapse = { viewModel.setPlayerExpanded(false) },
                                    onMiniPlayerDismiss = { 
                                        viewModel.setMiniPlayerVisible(false)
                                        viewModel.pause()
                                    },
                                    miniPlayerHeight = 80.dp,
                                    bottomOffset = bottomOffset,
                                    miniPlayerContent = {
                                        if (isMiniPlayerVisible && currentlyPlaying != null && currentRoute != "onboarding") {
                                            MiniPlayer(
                                                title = currentlyPlaying!!.title,
                                                artist = currentlyPlaying!!.artist,
                                                imageUrl = currentlyPlaying!!.artworkUri ?: currentlyPlaying!!.albumId?.let { "content://media/external/audio/albumart/$it" } ?: "",
                                                hazeState = hazeState, 
                                                tintTransparency = tintTransparency, 
                                                noiseFactor = noiseFactor, 
                                                isPlaying = isPlaying,
                                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                                onSkipNextClick = { viewModel.skipToNext() },
                                                onClick = { viewModel.setPlayerExpanded(true) },
                                                onDismiss = { 
                                                    viewModel.setMiniPlayerVisible(false)
                                                    viewModel.pause()
                                                },
                                                animatedVisibilityScope = null,
                                                horizontalPadding = 0.dp,
                                                enableSwipeToDismiss = false // Drag handled by bottom sheet
                                            )
                                        }
                                    },
                                    nowPlayingContent = {
                                        androidx.activity.compose.BackHandler(
                                            enabled = isPlayerExpanded
                                        ) {
                                            viewModel.setPlayerExpanded(false)
                                        }
                                        NowPlayingScreen(
                                            tintTransparency = tintTransparency,
                                            noiseFactor = noiseFactor,
                                            glowIntensity = glowIntensity,
                                            isDarkTheme = isDarkThemeForNowPlaying,
                                            onNavigateBack = { viewModel.setPlayerExpanded(false) },
                                            onNavigateToQueue = { 
                                                viewModel.setPlayerExpanded(false)
                                                navController.navigate("queue") 
                                            },
                                            onNavigateToAlbum = { albumId -> 
                                                viewModel.setPlayerExpanded(false)
                                                navController.navigate("album_details/$albumId") 
                                            },
                                            onNavigateToArtist = { artistId -> 
                                                viewModel.setPlayerExpanded(false)
                                                navController.navigate("artist_details/$artistId") 
                                            },
                                            onNavigateToShare = { type, id ->
                                                viewModel.setPlayerExpanded(false)
                                                navController.navigate("share?type=$type&id=$id")
                                            },
                                            onNavigateToEditMetadata = { trackId -> 
                                                viewModel.setPlayerExpanded(false)
                                                navController.navigate("edit_metadata/$trackId?readOnly=true") 
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Box(modifier = Modifier.fillMaxSize().applyHazeAndBackdrop(hazeState = hazeState)) {
                                            AnimatedGlowBackground(glowIntensity = glowIntensity, color = glowColor)
                                            NavHost(
                                                navController = navController,
                                                startDestination = startDest,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .then(
                                                        if (android.os.Build.VERSION.SDK_INT < 34) {
                                                            Modifier.pointerInput(Unit) {
                                                                var popped = false
                                                                var totalDrag = 0f
                                                                detectHorizontalDragGestures(
                                                                    onDragStart = {
                                                                        totalDrag = 0f
                                                                        popped = false
                                                                    },
                                                                    onHorizontalDrag = { change, dragAmount ->
                                                                        if (!popped) {
                                                                            totalDrag += dragAmount
                                                                            if (totalDrag > 100f) {
                                                                                val route = navController.currentDestination?.route
                                                                                if (route != "home" && route != "library" && route != "search") {
                                                                                    navController.popBackStack()
                                                                                    popped = true
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                )
                                                            }
                                                        } else {
                                                            Modifier
                                                        }
                                                    )
                                            ) {
                                                composable("onboarding") {
                                                    com.aeswox.arcmusic.ui.screens.OnboardingScreen(
                                                        viewModel = viewModel,
                                                        onFinish = {
                                                            navController.navigate("home") {
                                                                popUpTo("onboarding") { inclusive = true }
                                                            }
                                                        }
                                                    )
                                                }
                                                composable(
                                                    route = "home",
                                                    enterTransition = { NavTransitions.HomeEnter },
                                                    exitTransition = { NavTransitions.HomeExit },
                                                    popEnterTransition = { NavTransitions.HomePopEnter },
                                                    popExitTransition = { NavTransitions.HomePopExit }
                                                ) {
                                                    androidx.compose.runtime.CompositionLocalProvider(
                                                        LocalNavAnimatedVisibilityScope provides this
                                                    ) {
                                                        Box(modifier = Modifier.fillMaxSize()) {
                                                            MusicHomeScreen(
                                                                innerPadding = innerPadding,
                                                                currentTab = currentTab,
                                                                onTabSelected = { 
                                                                    currentTab = it
                                                                    if (it != 1) selectedGenre = null
                                                                },
                                                                selectedGenre = selectedGenre,
                                                                onGenreSelected = { selectedGenre = it },
                                                                isLibrarySelectionMode = isLibrarySelectionMode,
                                                                onLibrarySelectionModeChange = { isLibrarySelectionMode = it },
                                                                showCreatePlaylistFlow = showCreatePlaylistFlow,
                                                                onShowCreatePlaylistFlowChange = { showCreatePlaylistFlow = it },
                                                                bottomPadding = contentBottomPadding,
                                        onNavigateToCollectionGrowth = { navController.navigate("collection_growth") },
                                        onNavigateToCollectionHealth = { navController.navigate("collection_health") },
                                        tintTransparency = tintTransparency,
                                        noiseFactor = noiseFactor,
                                        glowIntensity = glowIntensity,
                                        onNavigateToSettings = { navController.navigate("settings") },
                                        onNavigateToAlbumDetails = { albumId -> navController.navigate("album_details/$albumId") },
                                        onNavigateToPlaylistDetails = { playlistId -> navController.navigate("playlist_details/$playlistId") },
                                        onNavigateToArtistDetails = { artistId -> navController.navigate("artist_details/$artistId") },
                                        onNavigateToShare = { type, id -> navController.navigate("share?type=$type&id=$id") },
                                        onNavigateToQueue = { navController.navigate("queue") },
                                        onNavigateToEditMetadata = { trackId -> navController.navigate("edit_metadata/$trackId?readOnly=true") },
                                        onNavigateToReceive = { navController.navigate("receive") },
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                                                composable(
                                                    route = "collection_growth",
                                                    enterTransition = { NavTransitions.SheetEnter },
                                                    exitTransition = { NavTransitions.SheetExit },
                                                    popEnterTransition = { NavTransitions.SheetPopEnter },
                                                    popExitTransition = { NavTransitions.SheetPopExit }
                                                ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                CollectionGrowthScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    glowIntensity = glowIntensity,
                                    viewModel = viewModel
                                )
                            }
                        }
                        composable(
                            route = "collection_health",
                            enterTransition = { NavTransitions.SheetEnter },
                            exitTransition = { NavTransitions.SheetExit },
                            popEnterTransition = { NavTransitions.SheetPopEnter },
                            popExitTransition = { NavTransitions.SheetPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                CollectionHealthScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToMissingContent = { navController.navigate("missing_content") },
                                    onNavigateToMissingArtwork = { navController.navigate("missing_artwork") },
                                    onNavigateToMissingLyrics = { navController.navigate("missing_lyrics") },
                                    onNavigateToMissingMetadata = { navController.navigate("missing_metadata") },
                                    onNavigateToDuplicateSongs = { navController.navigate("duplicate_songs") },
                                    onNavigateToCorruptedTags = { navController.navigate("corrupted_tags") },
                                    onNavigateToLowQualityFiles = { navController.navigate("low_quality_files") },
                                    glowIntensity = glowIntensity
                                )
                            }
                        }
                        composable(
                            route = "missing_content",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                MissingContentScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = viewModel
                                )
                            }
                        }
                        composable(
                            route = "missing_artwork",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                MissingArtworkScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "missing_lyrics",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                MissingLyricsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "missing_metadata",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                MissingMetadataScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEditMetadata = { trackId -> navController.navigate("edit_metadata/$trackId?readOnly=false") }
                                )
                            }
                        }
                        composable(
                            route = "duplicate_songs",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                DuplicateSongsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "corrupted_tags",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                CorruptedTagsScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToEditMetadata = { trackId -> navController.navigate("edit_metadata/$trackId?readOnly=false") }
                                )
                            }
                        }
                        composable(
                            route = "low_quality_files",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                LowQualityFilesScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }

                        composable(
                            "edit_metadata/{trackId}?readOnly={readOnly}",
                            arguments = listOf(
                                androidx.navigation.navArgument("trackId") { type = androidx.navigation.NavType.StringType },
                                androidx.navigation.navArgument("readOnly") { 
                                    type = androidx.navigation.NavType.BoolType
                                    defaultValue = false 
                                }
                            ),
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) { backStackEntry ->
                            val trackId = backStackEntry.arguments?.getString("trackId") ?: return@composable
                            val readOnly = backStackEntry.arguments?.getBoolean("readOnly") ?: false
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                EditMetadataScreen(
                                    trackId = trackId,
                                    viewModel = viewModel,
                                    isReadOnlyDefault = readOnly,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "artist_details/{artistId}",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) { backStackEntry ->
                            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                ArtistDetailsScreen(
                                    artistId = artistId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAlbum = { albumId -> navController.navigate("album_details/$albumId") },
                                    onNavigateToAllTracks = { aId -> navController.navigate("artist_tracks/$aId") },
                                    onNavigateToAllAlbums = { aId -> navController.navigate("artist_albums/$aId") },
                                    onNavigateToShare = { type, id -> navController.navigate("share?type=$type&id=$id") }
                                )
                            }
                        }
                        composable(
                            route = "artist_tracks/{artistId}",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) { backStackEntry ->
                            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                ArtistTracksScreen(
                                    artistId = artistId,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "artist_albums/{artistId}",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) { backStackEntry ->
                            val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                ArtistAlbumsScreen(
                                    artistId = artistId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToAlbum = { albumId -> navController.navigate("album_details/$albumId") }
                                )
                            }
                        }
                        composable(
                            route = "album_details/{albumId}",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) { backStackEntry ->
                            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                AlbumDetailsScreen(
                                    albumId = albumId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToArtist = { aId -> navController.navigate("artist_details/$aId") },
                                    onNavigateToAlbum = { aId -> navController.navigate("album_details/$aId") }
                                )
                            }
                        }
                        composable(
                            route = "playlist_details/{playlistId}",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) { backStackEntry ->
                            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                PlaylistDetailsScreen(
                                    playlistId = playlistId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToShare = { type, id -> navController.navigate("share?type=$type&id=$id") }
                                )
                            }
                        }
                        // Removed now_playing composable
                        composable(
                            route = "queue",
                            enterTransition = { NavTransitions.SheetEnter },
                            exitTransition = { NavTransitions.SheetExit },
                            popEnterTransition = { NavTransitions.SheetPopEnter },
                            popExitTransition = { NavTransitions.SheetPopExit }
                        ) {
                            QueueScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToLibrary = { 
                                    navController.navigate("home") { 
                                        popUpTo("home") { inclusive = false } 
                                    } 
                                }
                            )
                        }
                        composable(
                            route = "settings",
                            enterTransition = { NavTransitions.SheetEnter },
                            exitTransition = { NavTransitions.SheetExit },
                            popEnterTransition = { NavTransitions.SheetPopEnter },
                            popExitTransition = { NavTransitions.SheetPopExit }
                        ) {
                            val context = LocalContext.current
                            val settingsPermissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                listOf(Manifest.permission.READ_MEDIA_AUDIO)
                            } else {
                                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                            val settingsPermissionsState = rememberMultiplePermissionsState(permissions = settingsPermissionsList)
                            val lastFmApiKey by viewModel.lastFmApiKey.collectAsState()
                            val fanartTvApiKey by viewModel.fanartTvApiKey.collectAsState()
                            val coilDiskCacheLimitMb by viewModel.coilDiskCacheLimitMb.collectAsState()
                            val lyricsDisplayStyle by viewModel.lyricsDisplayStyle.collectAsState()
                            
                            val dynamicBottomPadding by remember(isMiniPlayerVisible, currentlyPlaying) {
                                derivedStateOf {
                                    val miniPlayerOffset = if (isMiniPlayerVisible && currentlyPlaying != null) 96.dp else 0.dp
                                    24.dp + miniPlayerOffset
                                }
                            }
                            
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                SettingsScreen(
                                    bottomPadding = dynamicBottomPadding,
                                    tintTransparency = tintTransparency,
                                    noiseFactor = noiseFactor,
                                    glowIntensity = glowIntensity,
                                    themeMode = themeMode,
                                    lightThemeForNowPlaying = lightThemeForNowPlaying,
                                    lyricsDisplayStyle = lyricsDisplayStyle,
                                    lastFmApiKey = lastFmApiKey,
                                    fanartTvApiKey = fanartTvApiKey,
                                    onThemeModeChange = { viewModel.setThemeMode(it) },
                                    onLightThemeForNowPlayingChange = { viewModel.setLightThemeForNowPlaying(it) },
                                    onLyricsDisplayStyleChange = { viewModel.setLyricsDisplayStyle(it) },
                                    onLastFmApiKeyChange = { viewModel.setLastFmApiKey(it) },
                                    onFanartTvApiKeyChange = { viewModel.setFanartTvApiKey(it) },
                                    coilDiskCacheLimitMb = coilDiskCacheLimitMb,
                                    onCoilDiskCacheLimitMbChange = { viewModel.setCoilDiskCacheLimitMb(it) },
                                    onNavigateToAppearance = { navController.navigate("appearance") },
                                    onNavigateToJigglePhysics = { navController.navigate("jiggle_physics") },
                                    onNavigateToEqualizer = { navController.navigate("equalizer") },
                                    onNavigateToMediaManagement = { navController.navigate("media_management") },
                                    onNavigateToLyricStyleSettings = { navController.navigate("lyrics_style_settings") },
                                    onNavigateToCanvasSettings = { navController.navigate("canvas_settings") },
                                    onNavigateBack = { navController.popBackStack() },
                                    onScanMediaStore = {
                                        if (settingsPermissionsState.allPermissionsGranted) {
                                            viewModel.scanMediaStore()
                                        } else {
                                            settingsPermissionsState.launchMultiplePermissionRequest()
                                        }
                                    },
                                    onRunDeepScan = { viewModel.runDeepScanBackground() },
                                    onTestEac3 = { viewModel.testEac3Playback(context) },
                                    onImportM3u = { uri -> viewModel.importM3uPlaylist(context, uri) },
                                    onExportM3u = { uri, playlistId -> viewModel.exportM3uPlaylist(context, uri, playlistId) },
                                    playlists = viewModel.libraryPlaylists.collectAsState().value,
                                    canvasEnabled = viewModel.canvasEnabled.collectAsState().value,
                                    onCanvasEnabledChange = { viewModel.setCanvasEnabled(it) }
                                )
                            }
                        }
                        composable(
                            route = "share?type={type}&id={id}",
                            arguments = listOf(
                                androidx.navigation.navArgument("type") { 
                                    type = androidx.navigation.NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                androidx.navigation.navArgument("id") { 
                                    type = androidx.navigation.NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            ),
                            enterTransition = { NavTransitions.SheetEnter },
                            exitTransition = { NavTransitions.SheetExit },
                            popEnterTransition = { NavTransitions.SheetPopEnter },
                            popExitTransition = { NavTransitions.SheetPopExit }
                        ) { backStackEntry ->
                            val payloadType = backStackEntry.arguments?.getString("type")
                            val payloadId = backStackEntry.arguments?.getString("id")
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                ShareScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onExternalShareClick = { /* TODO implement external share intent */ }
                                )
                            }
                        }
                        composable(
                            route = "receive",
                            enterTransition = { NavTransitions.SheetEnter },
                            exitTransition = { NavTransitions.SheetExit },
                            popEnterTransition = { NavTransitions.SheetPopEnter },
                            popExitTransition = { NavTransitions.SheetPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                ReceiveScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "appearance",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                        composable(
                            route = "lyrics_style_settings",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                val lyricsDisplayStyle by viewModel.lyricsDisplayStyle.collectAsState()
                                val lyricsShowControls by viewModel.lyricsShowControls.collectAsState()
                                val lyricsFadeSteepness by viewModel.lyricsFadeSteepness.collectAsState()
                                val lyricsFadeScaleCeiling by viewModel.lyricsFadeScaleCeiling.collectAsState()
                                val lyricsFadeDistanceSizing by viewModel.lyricsFadeDistanceSizing.collectAsState()
                                val lyricsBlurRadius by viewModel.lyricsBlurRadius.collectAsState()
                                val lyricsBlurDimming by viewModel.lyricsBlurDimming.collectAsState()

                                LyricStyleScreen(
                                    lyricsDisplayStyle = lyricsDisplayStyle,
                                    onLyricsDisplayStyleChange = { viewModel.setLyricsDisplayStyle(it) },
                                    lyricsShowControls = lyricsShowControls,
                                    onLyricsShowControlsChange = { viewModel.setLyricsShowControls(it) },
                                    lyricsFadeSteepness = lyricsFadeSteepness,
                                    onLyricsFadeSteepnessChange = { viewModel.setLyricsFadeSteepness(it) },
                                    lyricsFadeScaleCeiling = lyricsFadeScaleCeiling,
                                    onLyricsFadeScaleCeilingChange = { viewModel.setLyricsFadeScaleCeiling(it) },
                                    lyricsFadeDistanceSizing = lyricsFadeDistanceSizing,
                                    onLyricsFadeDistanceSizingChange = { viewModel.setLyricsFadeDistanceSizing(it) },
                                    lyricsBlurRadius = lyricsBlurRadius,
                                    onLyricsBlurRadiusChange = { viewModel.setLyricsBlurRadius(it) },
                                    lyricsBlurDimming = lyricsBlurDimming,
                                    onLyricsBlurDimmingChange = { viewModel.setLyricsBlurDimming(it) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "jiggle_physics",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                JigglePhysicsScreen(
                                    physicsMass = physicsMass,
                                    physicsStiffness = physicsStiffness,
                                    physicsDampingRatio = physicsDampingRatio,
                                    physicsAmplitude = physicsAmplitude,
                                    physicsGravity = physicsGravity,
                                    onPhysicsMassChange = { viewModel.setPhysicsMass(it) },
                                    onPhysicsStiffnessChange = { viewModel.setPhysicsStiffness(it) },
                                    onPhysicsDampingRatioChange = { viewModel.setPhysicsDampingRatio(it) },
                                    onPhysicsAmplitudeChange = { viewModel.setPhysicsAmplitude(it) },
                                    onPhysicsGravityChange = { viewModel.setPhysicsGravity(it) },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = "equalizer",
                            enterTransition = { NavTransitions.SheetEnter },
                            exitTransition = { NavTransitions.SheetExit },
                            popEnterTransition = { NavTransitions.SheetPopEnter },
                            popExitTransition = { NavTransitions.SheetPopExit }
                        ) {
                            EqualizerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "media_management",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                MediaManagementScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToExcludedFolders = { navController.navigate("excluded_folders") },
                                    viewModel = viewModel
                                )
                            }
                        }
                        composable(
                            route = "excluded_folders",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                ExcludedFoldersScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                    viewModel = viewModel
                                )
                            }
                        } // ExcludedFoldersScreen Box

                        composable(
                            route = "canvas_settings",
                            enterTransition = { NavTransitions.DetailEnter },
                            exitTransition = { NavTransitions.DetailExit },
                            popEnterTransition = { NavTransitions.DetailPopEnter },
                            popExitTransition = { NavTransitions.DetailPopExit }
                        ) {
                            val canvasEnabled by viewModel.canvasEnabled.collectAsState()
                            val canvasCacheLimitMb by viewModel.canvasCacheLimitMb.collectAsState()
                            
                            // We trigger a re-check of cache size when the screen opens
                            var currentCacheSizeMb by remember { mutableStateOf(0L) }
                            LaunchedEffect(Unit) {
                                val bytes = viewModel.canvasCacheManager.getCacheSizeBytes()
                                currentCacheSizeMb = bytes / (1024 * 1024)
                            }
                            
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                CanvasSettingsScreen(
                                    canvasEnabled = canvasEnabled,
                                    onCanvasEnabledChange = { viewModel.setCanvasEnabled(it) },
                                    cacheLimitMb = canvasCacheLimitMb,
                                    onCacheLimitMbChange = { viewModel.setCanvasCacheLimitMb(it) },
                                    currentCacheSizeMb = currentCacheSizeMb,
                                    onClearCache = {
                                        viewModel.canvasCacheManager.clearCache()
                                        currentCacheSizeMb = 0L
                                    },
                                    onFetchCanvases = {
                                        val intent = android.content.Intent(this@MainActivity, com.aeswox.arcmusic.service.CanvasFetchService::class.java)
                                        startService(intent)
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    } // NavHost
                                        } // Box (applyHazeAndBackdrop)
                                        
                                        // â”€â”€ Gradient scrim behind bottom chrome â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                                        // Mirrors Rhythm's LocalNavigation approach: the scrim height is animated
                                        // so it grows/shrinks as the nav bar and miniplayer come and go.
                                        val miniPlayerVisible = isMiniPlayerVisible && currentlyPlaying != null
                                        val miniPlayerH = 80.dp
                                        val navBarH = 70.dp
                                        val rawGradientHeight = when {
                                            isNavBarVisible && miniPlayerVisible -> navBarH + 16.dp + miniPlayerH + 32.dp
                                            isNavBarVisible -> navBarH + 32.dp
                                            miniPlayerVisible -> miniPlayerH + 32.dp
                                            else -> 0.dp
                                        } + innerPadding.calculateBottomPadding()
                                        val animGradientHeight by animateDpAsState(
                                            targetValue = rawGradientHeight,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "bottomChromeGradientHeight"
                                        )
                                        val bottomChromeVisible = isNavBarVisible || miniPlayerVisible
                                        val gradientAlpha by animateFloatAsState(
                                            targetValue = if (bottomChromeVisible) 1f else 0f,
                                            animationSpec = tween(durationMillis = 220),
                                            label = "bottomChromeGradientAlpha"
                                        )
                                        BottomChromeGradient(
                                            height = animGradientHeight,
                                            alpha = gradientAlpha,
                                            modifier = Modifier.align(Alignment.BottomCenter)
                                        )

                                        // â”€â”€ Navigation bar (animated show/hide) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
                                        // ENTER: slide up from below + fade with a medium-bouncy spring (satisfying pop).
                                        // EXIT:  slide down + fade with a no-bounce spring (snappy disappear).
                                        AnimatedVisibility(
                                            visible = isNavBarVisible,
                                            modifier = Modifier.align(Alignment.BottomCenter),
                                            enter = slideInVertically(
                                                initialOffsetY = { fullHeight -> fullHeight / 2 },
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            ) + fadeIn(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            ),
                                            exit = slideOutVertically(
                                                targetOffsetY = { fullHeight -> fullHeight / 2 },
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            ) + fadeOut(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp + innerPadding.calculateBottomPadding()),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                com.aeswox.arcmusic.BottomNavigation(
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
                                        }
                                    } // Box (line 287)
                                } // PlayerBottomSheet trailing lambda
                            
                            if (showCreatePlaylistFlow) {
                                val tracks by viewModel.libraryTracks.collectAsState()
                                com.aeswox.arcmusic.CreatePlaylistFlow(
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
            }
        }
    }
}
        }
    }

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun MusicHomeScreen(
    innerPadding: PaddingValues = PaddingValues(0.dp),
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    selectedGenre: String?,
    onGenreSelected: (String?) -> Unit,
    isLibrarySelectionMode: Boolean,
    onLibrarySelectionModeChange: (Boolean) -> Unit,
    showCreatePlaylistFlow: Boolean,
    onShowCreatePlaylistFlowChange: (Boolean) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onNavigateToCollectionGrowth: () -> Unit = {},
    onNavigateToCollectionHealth: () -> Unit = {},
    onNavigateToPlaylistDetails: (String) -> Unit = {},
    onNavigateToShare: (String, String) -> Unit = { _, _ -> },
    onNavigateToReceive: () -> Unit = {},
    modifier: Modifier = Modifier,
    tintTransparency: Float = 0.4f,
    noiseFactor: Float = 0.06f,
    glowIntensity: Float = 0.6f,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAlbumDetails: (String) -> Unit = {},
    onNavigateToArtistDetails: (String) -> Unit = {},
    onNavigateToQueue: () -> Unit = {},
    onNavigateToEditMetadata: (String) -> Unit = {},
    viewModel: MusicViewModel = hiltViewModel()
) {
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    val currentlyPlayingEntity by viewModel.currentlyPlaying.collectAsState()
    val currentSong = currentlyPlayingEntity
    val artworkUrl = currentSong?.artworkUri ?: currentSong?.albumId?.let { "content://media/external/audio/albumart/$it" }
    val glowColor by rememberDominantColor(imageUrl = artworkUrl, defaultColor = Color(0xFF5E90A7))
    val libraryTracks by viewModel.libraryTracks.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isLibraryLoaded by viewModel.isLibraryLoaded.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isMiniPlayerVisible by viewModel.isMiniPlayerVisible.collectAsState()

    // Bottom padding is provided by MainActivity now

    LaunchedEffect(currentlyPlayingEntity, isPlaying) {
        if (currentlyPlayingEntity != null && isPlaying) {
            viewModel.setMiniPlayerVisible(true)
        }
    }
    
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            viewModel.setNavBarVisible(false)
            viewModel.setNavBarHeight(0.dp)
        }
    }

    val permissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsList)
    
    androidx.activity.compose.BackHandler(
        enabled = showCreatePlaylistFlow || isLibrarySelectionMode || selectedGenre != null || currentTab != 0
    ) {
        when {
            showCreatePlaylistFlow -> onShowCreatePlaylistFlowChange(false)
            isLibrarySelectionMode -> onLibrarySelectionModeChange(false)
            selectedGenre != null -> onGenreSelected(null)
            currentTab != 0 -> onTabSelected(0)
        }
    }

    val onSongClick: (Track, List<Track>?) -> Unit = { song, queue ->
        viewModel.setCurrentlyPlaying(song, queue)
        viewModel.setMiniPlayerVisible(true)
    }

    val previousTab = remember { androidx.compose.runtime.mutableIntStateOf(currentTab) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            )
    ) {
        AnimatedContent(
            targetState = currentTab,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val goingRight = targetState > initialState
                val enter = fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(200)
                ) + slideInHorizontally(
                    initialOffsetX = { if (goingRight) it else -it },
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                )
                val exit = fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(180)
                ) + slideOutHorizontally(
                    targetOffsetX = { if (goingRight) -it else it },
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                )
                enter.togetherWith(exit)
            },
            label = "tab_switch"
        ) { tab ->
            when (tab) {
                0 -> {
                    if (isLibraryLoaded) {
                        if (libraryTracks.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 24.dp, bottom = bottomPadding)
                            ) {
                                Header(modifier = Modifier.padding(horizontal = 24.dp), onSettingsClick = onNavigateToSettings, onTitleLongClick = onNavigateToReceive)
                                
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
                                modifier = Modifier.physicsBounceOverscroll()
                                    .fillMaxSize()
                            ) {
                                item {
                                    Header(modifier = Modifier.padding(horizontal = 24.dp), onSettingsClick = onNavigateToSettings, onTitleLongClick = onNavigateToReceive)
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
                                    RecommendedDownloadsSection(onNavigateToCollectionGrowth = onNavigateToCollectionGrowth)
                                }
                                item {
                                    val healthState by viewModel.healthState.collectAsState()
                                    CollectionHealthSection(
                                        healthScore = healthState.healthScore,
                                        onClick = onNavigateToCollectionHealth
                                    )
                                }
                                item {
                                    val stats by viewModel.listeningStats.collectAsState()
                                    ListeningStatsSection(stats = stats, onClick = { onTabSelected(3) })
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
                                onNavigateBack = { onGenreSelected(null) }
                            )
                        } else {
                            SearchScreenContent(viewModel = viewModel, bottomPadding = bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onGenreClick = { onGenreSelected(it) })
                        }
                    }
                    2 -> {
                        LibraryScreenContent(bottomPadding = if (isLibrarySelectionMode) 100.dp else bottomPadding, onNavigateToAlbumDetails = onNavigateToAlbumDetails, onNavigateToPlaylistDetails = onNavigateToPlaylistDetails, onNavigateToArtistDetails = onNavigateToArtistDetails, onNavigateToShare = onNavigateToShare, onSelectionModeChange = { onLibrarySelectionModeChange(it) }, onCreatePlaylistClick = { onShowCreatePlaylistFlowChange(true) })
                    }
                    3 -> {
                        val stats by viewModel.listeningStats.collectAsState()
                        ListeningStatsScreenContent(stats = stats, bottomPadding = bottomPadding, onNavigateBack = { onTabSelected(0) })
                    }
                }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun Header(modifier: Modifier = Modifier, title: String? = "Arc Music", fontSize: androidx.compose.ui.unit.TextUnit = 34.sp, onSettingsClick: () -> Unit = {}, onBackClick: () -> Unit = {}, onTitleLongClick: () -> Unit = {}) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (title != null) {
            androidx.compose.material3.Text(
                text = title, 
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                    fontSize = fontSize,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ), 
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                modifier = androidx.compose.ui.Modifier.combinedClickable(
                    onClick = { /* Do nothing on normal click */ },
                    onLongClick = onTitleLongClick
                )
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            JellyIconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Down",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        JellyIconButton(onClick = onSettingsClick) {
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
                .aspectRatio(1.3f)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(36.dp))
                .jellyClick { showLyrics = !showLyrics }
        ) {
            AsyncImage(
                model = currentSong.artworkUri ?: currentSong.albumId?.let { "content://media/external/audio/albumart/$it" },
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
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentSong.title, 
                            style = MaterialTheme.typography.headlineLarge, 
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentSong.artist, 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        com.aeswox.arcmusic.ui.components.PlayPauseMorphIcon(
                            isPlaying = isPlaying,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    } else {
        val suggestedSong = randomPicks.firstOrNull()
        if (suggestedSong == null) return
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1.3f)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(36.dp))
        ) {
            AsyncImage(
                model = suggestedSong.artworkUri ?: suggestedSong.albumId?.let { "content://media/external/audio/albumart/$it" },
                contentDescription = suggestedSong.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent, 
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
            ) {
                Text(
                    text = suggestedSong.title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestedSong.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppPrimaryButton(
                        text = "Play",
                        onClick = { onPlayClick(suggestedSong, randomPicks) },
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (suggestedSong.year != null && suggestedSong.year > 0) {
                        Text(
                            text = suggestedSong.year.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    val badgeRes = suggestedSong.getQualityBadgeResId()
                    if (badgeRes != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = badgeRes),
                            contentDescription = "Quality",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
        syncedLines?.map { it.line } ?: plainLines ?: listOf("â™ª")
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

    val activeLine = lines.getOrElse(activeLineIndex) { "â™ª" }
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
modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),

            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs) { song ->
                AsyncImage(
                    model = song.artworkUri ?: song.albumId?.let { "content://media/external/audio/albumart/$it" },
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .jellyClick { onSongClick(song, songs) }
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
            .jellyClick { onSongClick(song) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artworkUri ?: song.albumId?.let { "content://media/external/audio/albumart/$it" },
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
        JellyIconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.MoreVert, 
                contentDescription = "More",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecommendedDownloadsSection(onNavigateToCollectionGrowth: () -> Unit = {}, modifier: Modifier = Modifier) {
    val viewModel: MusicViewModel = hiltViewModel()
    val cards by viewModel.homescreenRecommendations.collectAsState()
    
    if (cards.isEmpty()) {
        Column(
            modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Recommended Downloads", 
                style = MaterialTheme.typography.headlineMedium, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No new songs or trending tracks right now. Favorite more artists or listen to more music!", 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .jellyClick { onNavigateToCollectionGrowth() }
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
                cards.forEach { card ->
                    RecommendedDownloadItem(card, viewModel)
                }
            }
        }
    }
}

@Composable
fun RecommendedDownloadItem(card: GrowthCard, viewModel: MusicViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val scope = rememberCoroutineScope()
    
    val title = when (card) {
        is GrowthCard.NewSong -> card.trackTitle
        is GrowthCard.Trending -> card.trackTitle
        is GrowthCard.NewRelease -> card.albumTitle
        is GrowthCard.MissingTracks -> card.albumTitle
        is GrowthCard.CompleteCollection -> card.missingAlbumTitle
        is GrowthCard.Discovery -> card.suggestedArtistName
    }
    
    val artist = when (card) {
        is GrowthCard.NewSong -> card.artistName
        is GrowthCard.Trending -> card.artistName
        is GrowthCard.NewRelease -> card.artistName
        is GrowthCard.MissingTracks -> card.artistName
        is GrowthCard.CompleteCollection -> card.artistName
        is GrowthCard.Discovery -> "Similar to ${card.becauseOfArtist}"
    }
    
    val imageUrl = card.imageUrl

    val badgeText = when (card) {
        is GrowthCard.NewSong -> "NEW "
        is GrowthCard.Trending -> "TRENDING "
        is GrowthCard.NewRelease -> "ALBUM "
        is GrowthCard.MissingTracks -> "MISSING "
        is GrowthCard.CompleteCollection -> "COMPLETE "
        is GrowthCard.Discovery -> "ARTIST "
    }
    
    val badgeColor = when (card) {
        is GrowthCard.NewSong -> MaterialTheme.colorScheme.primary
        is GrowthCard.Trending -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }

    val downloadType = when (card) {
        is GrowthCard.NewSong, is GrowthCard.Trending -> SpotiFlacDownloadType.TRACK
        is GrowthCard.NewRelease, is GrowthCard.MissingTracks, is GrowthCard.CompleteCollection -> SpotiFlacDownloadType.ALBUM
        is GrowthCard.Discovery -> SpotiFlacDownloadType.ARTIST
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .jellyClick { 
                val encodedQuery = java.net.URLEncoder.encode("$title $artist", "UTF-8")
                uriHandler.openUri("https://music.youtube.com/search?q=$encodedQuery")
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = badgeText, 
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                    color = badgeColor
                )
                Text(
                    text = artist, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        JellyIconButton(onClick = {
            val query = if (downloadType == SpotiFlacDownloadType.ARTIST) title else "$title $artist"
            performSpotiFlacDownload(context, scope, viewModel, query, downloadType)
        }) {
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
        else                              -> "â€”"
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
                .jellyClick { onClick() }
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // â”€â”€ Row 1: Top Artist | Favorite Genre â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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
                                text = "â€”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // â”€â”€ Row 2: Weekly Listening | Mini bar chart â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    
    // Reset selected filter when search query transitions between empty and active
    LaunchedEffect(searchQuery.isEmpty()) {
        selectedFilter = "All"
    }
    
    LazyColumn(
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier.physicsBounceOverscroll().fillMaxSize()
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
            style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                fontSize = 34.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ), 
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            JellyIconButton(onClick = { }) {
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
            JellyIconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            JellyIconButton(onClick = { onQueryChange("") }) {
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
        modifier = modifier.physicsBounceOverscroll(isHorizontal = true).fillMaxWidth()
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                    .jellyClick { onFilterSelected(filter) }
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
                    modifier = Modifier.jellyClick { onClearAll() }
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
            .jellyClick { onClick() }
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
                .jellyClick { onClick() }
        ) {
            val fallbackImage = R.drawable.ic_default_artwork
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
                            .jellyClick { onClick() },
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
            style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ), 
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.jellyClick { }) {
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
                val fallbackImage = R.drawable.ic_default_artwork
                tracks.forEach { track ->
                    val durationMs = track.durationMs
                    val minutes = durationMs / 1000 / 60
                    val seconds = (durationMs / 1000 % 60).toString().padStart(2, '0')
                    SongResultItem(
                        title = track.title, 
                        artist = track.artist, 
                        duration = "$minutes:$seconds", 
                        imageUrl = track.artworkUri ?: fallbackImage,
                        qualityBadgeResId = null,
                        isExplicit = track.isExplicit,
                        onClick = { onTrackClick(track) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun SongResultItem(title: String, artist: String, duration: String, imageUrl: Any?, isActive: Boolean = false, isSelectionMode: Boolean = false, isSelected: Boolean = false, qualityBadgeResId: Int? = null, isExplicit: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isExplicit) {
                    Spacer(modifier = Modifier.width(6.dp))
                    com.aeswox.arcmusic.ExplicitBadge()
                }
            }
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (qualityBadgeResId != null) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = qualityBadgeResId),
                    contentDescription = "Audio Quality Badge",
                    modifier = Modifier.fillMaxWidth(0.5f).alpha(0.7f),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AlbumsResultSection(albums: List<com.aeswox.arcmusic.db.entities.Album>, modifier: Modifier = Modifier, onNavigateToAlbumDetails: (String) -> Unit = {}) {
    Column(modifier = modifier) {
        SectionHeader(title = "Albums")
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),

            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums) { album ->
                val fallbackImage = R.drawable.ic_default_artwork
                val yearStr = if (album.year != null && album.year > 0) "${album.year} â€¢ " else ""
                val subtitle = "$yearStr${album.trackCount} songs"
                AlbumResultItem(album.title, subtitle, album.artworkUri ?: fallbackImage, onClick = { onNavigateToAlbumDetails(album.title) })
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun AlbumResultItem(title: String, year: String, imageUrl: Any?, modifier: Modifier = Modifier.width(140.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
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
modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),

            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(artists) { artist ->
                val fallbackImage = R.drawable.ic_default_artwork
                ArtistResultItem(artist.name, artist.photoUri ?: fallbackImage, artist.isFavorite, onClick = { onNavigateToArtistDetails(artist.name) })
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun ArtistResultItem(name: String, imageUrl: Any?, isVerified: Boolean = false, modifier: Modifier = Modifier.width(100.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
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
modifier = Modifier.physicsBounceOverscroll(isHorizontal = true),

            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(playlists) { playlist ->
                val fallbackImage = R.drawable.ic_default_artwork
                PlaylistResultItem(title = playlist.name, subtitle = "Playlist", imageUrl = playlist.coverArtUri ?: fallbackImage, onClick = { onNavigateToPlaylistDetails(playlist.name) })
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun PlaylistResultItem(title: String, subtitle: String, imageUrl: Any?, modifier: Modifier = Modifier.width(280.dp), isSelectionMode: Boolean = false, isSelected: Boolean = false, onLongClick: (() -> Unit)? = null, onClick: () -> Unit = {}) {
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
fun CollectionHealthSection(healthScore: Int = 100, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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
            modifier = Modifier.padding(horizontal = 24.dp).jellyClick { onClick() }
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
                            progress = { healthScore / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onSurface,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            strokeWidth = 3.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(
                            text = "${healthScore}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column {
                        Text(
                            text = "${healthScore}%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (healthScore == 100) "Your collection is perfect!" else "Your collection is in great\nshape.",
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
            .jellyClick { }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

