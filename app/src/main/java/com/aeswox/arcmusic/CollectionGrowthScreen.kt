package com.aeswox.arcmusic

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.combinedClickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionGrowthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    glowIntensity: Float = 0.6f
) {
    val growthState by viewModel.growthState.collectAsState()
    var selectedDiscoveryCard by remember { mutableStateOf<GrowthCard.Discovery?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadCollectionGrowth()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Collection Growth",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            when (val state = growthState) {
                is CollectionGrowthUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Scanning your library…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is CollectionGrowthUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nothing to show yet",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Favorite some artists on their Artist page to start tracking new releases, gaps, and discoveries.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                is CollectionGrowthUiState.Success -> {
                    val completeCollectionCards = state.cards.filterIsInstance<GrowthCard.CompleteCollection>().distinctBy { it.artistName + it.missingAlbumTitle }
                    val newReleaseCards = state.cards.filterIsInstance<GrowthCard.NewRelease>().distinctBy { it.artistName + it.albumTitle }
                    val missingTracksCards = state.cards.filterIsInstance<GrowthCard.MissingTracks>().distinctBy { it.artistName + it.albumTitle }
                    val discoveryCards = state.cards.filterIsInstance<GrowthCard.Discovery>().distinctBy { it.suggestedArtistName + it.becauseOfArtist }
                    val newSongCards = state.cards.filterIsInstance<GrowthCard.NewSong>().distinctBy { it.artistName + it.trackTitle }
                    val trendingCards = state.cards.filterIsInstance<GrowthCard.Trending>().distinctBy { it.artistName + it.trackTitle }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (completeCollectionCards.isNotEmpty()) {
                            item { SectionHeader("Almost Complete", modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)) }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(completeCollectionCards, key = { card -> "cc_${card.artistName}_${card.missingAlbumTitle}" }) { card ->
                                        CompleteCollectionCard(
                                            card = card,
                                            onDismiss = { viewModel.dismissGrowthCard(card) },
                                            onDownloadClick = { performSpotiFlacDownload(context, scope, viewModel, "${card.missingAlbumTitle} ${card.artistName}", SpotiFlacDownloadType.ALBUM) },
                                            onDownloadLongClick = { performSpotiFlacManualSearch(context, "${card.missingAlbumTitle} ${card.artistName}") },
                                            modifier = Modifier.width(300.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (newReleaseCards.isNotEmpty()) {
                            item { SectionHeader("New Releases", modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)) }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(newReleaseCards, key = { card -> "nr_${card.artistName}_${card.albumTitle}" }) { card ->
                                        NewReleaseCard(
                                            card = card,
                                            onDismiss = { viewModel.dismissGrowthCard(card) },
                                            onDownloadClick = { performSpotiFlacDownload(context, scope, viewModel, "${card.albumTitle} ${card.artistName}", SpotiFlacDownloadType.ALBUM) },
                                            onDownloadLongClick = { performSpotiFlacManualSearch(context, "${card.albumTitle} ${card.artistName}") },
                                            modifier = Modifier.width(300.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (missingTracksCards.isNotEmpty()) {
                            item { SectionHeader("Missing Tracks", modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)) }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(missingTracksCards, key = { card -> "mt_${card.artistName}_${card.albumTitle}" }) { card ->
                                        MissingTracksCard(
                                            card = card,
                                            onDismiss = { viewModel.dismissGrowthCard(card) },
                                            onDownloadClick = { performSpotiFlacDownload(context, scope, viewModel, "${card.albumTitle} ${card.artistName}", SpotiFlacDownloadType.ALBUM) },
                                            onDownloadLongClick = { performSpotiFlacManualSearch(context, "${card.albumTitle} ${card.artistName}") },
                                            modifier = Modifier.width(300.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (discoveryCards.isNotEmpty()) {
                            item { SectionHeader("Discover", modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)) }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(discoveryCards, key = { card -> "disc_${card.suggestedArtistName}_${card.becauseOfArtist}" }) { card ->
                                        DiscoveryChip(card = card, onClick = { selectedDiscoveryCard = card })
                                    }
                                }
                            }
                        }

                        if (newSongCards.isNotEmpty()) {
                            item { SectionHeader("Recommended Downloads", modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)) }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(newSongCards, key = { card -> "ns_${card.artistName}_${card.trackTitle}" }) { card ->
                                        NewSongCard(
                                            card = card,
                                            onDismiss = { viewModel.dismissGrowthCard(card) },
                                            onDownloadClick = { performSpotiFlacDownload(context, scope, viewModel, "${card.trackTitle} ${card.artistName}", SpotiFlacDownloadType.TRACK) },
                                            onDownloadLongClick = { performSpotiFlacManualSearch(context, "${card.trackTitle} ${card.artistName}") },
                                            modifier = Modifier.width(300.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (trendingCards.isNotEmpty()) {
                            item { SectionHeader("Trending", modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)) }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(trendingCards, key = { card -> "tr_${card.artistName}_${card.trackTitle}" }) { card ->
                                        TrendingCard(
                                            card = card,
                                            onDismiss = { viewModel.dismissGrowthCard(card) },
                                            onDownloadClick = { performSpotiFlacDownload(context, scope, viewModel, "${card.trackTitle} ${card.artistName}", SpotiFlacDownloadType.TRACK) },
                                            onDownloadLongClick = { performSpotiFlacManualSearch(context, "${card.trackTitle} ${card.artistName}") },
                                            modifier = Modifier.width(300.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedDiscoveryCard != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { selectedDiscoveryCard = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DiscoveryBottomSheetContent(
                    card = selectedDiscoveryCard!!,
                    onDismiss = { 
                        viewModel.dismissGrowthCard(selectedDiscoveryCard!!)
                        selectedDiscoveryCard = null
                    },
                    onDownloadClick = { performSpotiFlacDownload(context, scope, viewModel, selectedDiscoveryCard!!.suggestedArtistName, SpotiFlacDownloadType.ARTIST) },
                    onDownloadLongClick = { performSpotiFlacManualSearch(context, selectedDiscoveryCard!!.suggestedArtistName) },
                    onClose = { selectedDiscoveryCard = null }
                )
            }
        }
    }
}



// ---------------------------------------------------------------------------
// Shared helper: launch a search intent
// ---------------------------------------------------------------------------

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DownloadButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}

enum class SpotiFlacDownloadType { TRACK, ALBUM, ARTIST }

private fun performSpotiFlacDownload(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    viewModel: MusicViewModel,
    query: String,
    type: SpotiFlacDownloadType
) {
    android.widget.Toast.makeText(context, "Searching for download...", android.widget.Toast.LENGTH_SHORT).show()
    scope.launch {
        val url = when (type) {
            SpotiFlacDownloadType.TRACK -> viewModel.getTrackDownloadUrl(query)
            SpotiFlacDownloadType.ALBUM -> viewModel.getAlbumDownloadUrl(query)
            SpotiFlacDownloadType.ARTIST -> viewModel.getArtistDownloadUrl(query)
        }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            if (url != null && url.startsWith("error:")) {
                android.widget.Toast.makeText(context, "Debug Error: ${url.removePrefix("error:")}", android.widget.Toast.LENGTH_LONG).show()
                return@withContext
            }
            
            val finalUrl = url ?: query
            
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Search Query", finalUrl)
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "Query copied to clipboard.", android.widget.Toast.LENGTH_SHORT).show()
            }
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                setType("text/plain")
                putExtra(Intent.EXTRA_TEXT, finalUrl)
                setPackage("com.zarz.spotiflac")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "SpotiFLAC not installed.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun performSpotiFlacManualSearch(
    context: android.content.Context,
    query: String
) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Search Query", query)
    clipboard.setPrimaryClip(clip)
    android.widget.Toast.makeText(context, "Query copied. Please paste in search.", android.widget.Toast.LENGTH_LONG).show()

    val launchIntent = context.packageManager.getLaunchIntentForPackage("com.zarz.spotiflac")
    if (launchIntent != null) {
        try {
            context.startActivity(launchIntent)
        } catch (e: Exception) {}
    } else {
        android.widget.Toast.makeText(context, "SpotiFLAC not installed.", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun launchYouTubeMusicSearch(context: android.content.Context, query: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=${Uri.encode(query)}"))
    try { context.startActivity(intent) } catch (e: Exception) {}
}

// ---------------------------------------------------------------------------
// Card composables
// ---------------------------------------------------------------------------

@Composable
fun DismissCircularButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(12.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .size(32.dp)
    ) {
        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun CompleteCollectionCard(
    card: GrowthCard.CompleteCollection,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            DismissCircularButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Album, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("COMPLETE COLLECTION", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = card.missingAlbumTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "You own ${card.ownedCount} of ${card.totalCount} ${card.artistName} albums.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DownloadButton(
                        onClick = onDownloadClick,
                        onLongClick = onDownloadLongClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun NewReleaseCard(
    card: GrowthCard.NewRelease,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            DismissCircularButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("NEW ${card.releaseType.uppercase()}", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = card.albumTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "New from ${card.artistName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DownloadButton(
                        onClick = onDownloadClick,
                        onLongClick = onDownloadLongClick,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { launchYouTubeMusicSearch(context, "${card.albumTitle} ${card.artistName}") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242), contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("YouTube")
                    }
                }
            }
        }
    }
}

@Composable
fun MissingTracksCard(
    card: GrowthCard.MissingTracks,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progress = if (card.totalCount > 0) card.ownedCount.toFloat() / card.totalCount.toFloat() else 0f

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "MISSING TRACKS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (card.imageUrl != null) {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = card.albumTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${card.missingCount} song${if (card.missingCount != 1) "s" else ""} missing by ${card.artistName}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${card.ownedCount} Collected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${card.totalCount} Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { launchYouTubeMusicSearch(context, "${card.albumTitle} ${card.artistName}") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search YouTube", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        DismissCircularButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd))
    }
}
}


@Composable
fun DiscoveryChip(card: GrowthCard.Discovery, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp).clickable { onClick() }
    ) {
        AsyncImage(
            model = card.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            fallback = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_myplaces),
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = card.suggestedArtistName,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DiscoveryBottomSheetContent(
    card: GrowthCard.Discovery,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadLongClick: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("DISCOVERY", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = card.suggestedArtistName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Since you love ${card.becauseOfArtist}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!card.sharedGenre.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Shared Genre: ${card.sharedGenre}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DownloadButton(
                onClick = { 
                    onDownloadClick()
                    onClose()
                },
                onLongClick = {
                    onDownloadLongClick()
                    onClose()
                }
            )
            Button(
                onClick = { 
                    launchYouTubeMusicSearch(context, card.suggestedArtistName) 
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000), contentColor = Color.White)
            ) {
                Icon(Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search YouTube")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { 
            onDismiss() 
        }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dismiss Recommendation")
        }
    }
}

@Composable
fun NewSongCard(
    card: GrowthCard.NewSong,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            DismissCircularButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd))
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "NEW SONG • ${card.releaseDateStr}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.trackTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DownloadButton(
                        onClick = onDownloadClick,
                        onLongClick = onDownloadLongClick,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { launchYouTubeMusicSearch(context, "${card.trackTitle} ${card.artistName}") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242), contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("YouTube", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingCard(
    card: GrowthCard.Trending,
    onDismiss: () -> Unit,
    onDownloadClick: () -> Unit,
    onDownloadLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )
            DismissCircularButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd))
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                if (!card.matchedGenre.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TRENDING • ${card.matchedGenre.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TRENDING",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Text(
                    text = card.trackTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DownloadButton(
                        onClick = onDownloadClick,
                        onLongClick = onDownloadLongClick,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { launchYouTubeMusicSearch(context, "${card.trackTitle} ${card.artistName}") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242), contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("YouTube", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
