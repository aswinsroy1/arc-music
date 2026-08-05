package com.aeswox.arcmusic

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingContentScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = hiltViewModel()
) {
    val uiState by viewModel.missingContentUiState.collectAsState()
    var currentTab by remember { mutableIntStateOf(0) } // 0 for Tracks, 1 for Albums, 2 for Singles
    
    LaunchedEffect(Unit) {
        viewModel.loadMissingContent()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Missing Content", 
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
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "Discover tracks and albums from your favorite artists that aren't in your local library yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        label = { Text("Partial Albums", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                            selectedLabelColor = MaterialTheme.colorScheme.surface,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = CircleShape,
                        border = null
                    )
                    FilterChip(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        label = { Text("Entire Albums", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                            selectedLabelColor = MaterialTheme.colorScheme.surface,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = CircleShape,
                        border = null
                    )
                    FilterChip(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        label = { Text("Singles", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                            selectedLabelColor = MaterialTheme.colorScheme.surface,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = CircleShape,
                        border = null
                    )
                }

                when (val state = uiState) {
                    is MissingContentUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is MissingContentUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is MissingContentUiState.Success -> {
                        val itemsGrouped = when (currentTab) {
                            0 -> state.missingTracks
                            1 -> state.missingAlbums
                            else -> state.missingSingles
                        }
                        if (itemsGrouped.isEmpty()) {
                            val tabName = when (currentTab) {
                                0 -> "partial missing albums"
                                1 -> "entire missing albums"
                                else -> "missing singles"
                            }
                            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No $tabName found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                itemsGrouped.forEach { (artistName, items) ->
                                    item(key = artistName) {
                                        Text(
                                            text = artistName,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                                        )
                                    }
                                    
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            items.forEach { missingItem ->
                                                MissingItemRow(item = missingItem)
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
    }
}

@Composable
fun MissingItemRow(item: MissingContentItem) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header row: thumbnail, title/subtitle, chevron or action button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subText = when {
                    item.isSingle && item.isAlbum -> "Single"
                    item.isSingle -> "${item.missingCount} missing tracks"
                    item.isAlbum -> "Full Album"
                    else -> "${item.missingCount} missing tracks"
                }
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Entire albums: show a YouTube Music button directly
            // Partial albums: show expand/collapse chevron
            if (item.isAlbum) {
                IconButton(onClick = {
                    val query = "${item.title} album ${item.artistName}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=${Uri.encode(query)}"))
                    try { context.startActivity(intent) } catch (e: Exception) {}
                }) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "Search YouTube Music",
                        tint = Color(0xFFFF0000)
                    )
                }
            } else {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand missing tracks",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Expanded track list — only for partial albums
        AnimatedVisibility(visible = expanded && !item.isAlbum) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
            ) {
                if (item.missingTrackNames.isEmpty()) {
                    Text(
                        text = "Track names unavailable — tap below to search the album",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 80.dp, bottom = 8.dp)
                    )
                    // Show album-level search buttons as fallback
                    Row(
                        modifier = Modifier.padding(start = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(onClick = {
                            val query = "${item.title} ${item.artistName}"
                            val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=${Uri.encode(query)}"))
                            try { context.startActivity(ytIntent) } catch (e: Exception) {}
                        }) {
                            Icon(Icons.Default.SmartDisplay, contentDescription = "YouTube Music", tint = Color(0xFFFF0000), modifier = Modifier.size(20.dp))
                        }
                        FilledTonalIconButton(onClick = {
                            val query = "album:${item.title} artist:${item.artistName}"
                            val spIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/${Uri.encode(query)}"))
                            try { context.startActivity(spIntent) } catch (e: Exception) {}
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Spotify", tint = Color(0xFF1DB954), modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    item.missingTrackNames.forEachIndexed { index, trackName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 80.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. $trackName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                            // YouTube Music search for this specific track
                            IconButton(
                                onClick = {
                                    val ytQuery = "$trackName ${item.artistName}"
                                    val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=${Uri.encode(ytQuery)}"))
                                    try { context.startActivity(ytIntent) } catch (e: Exception) {}
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartDisplay,
                                    contentDescription = "Search on YouTube Music",
                                    tint = Color(0xFFFF0000),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Spotify search for this specific track
                            IconButton(
                                onClick = {
                                    val spQuery = "$trackName ${item.artistName}"
                                    val spIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/${Uri.encode(spQuery)}"))
                                    try { context.startActivity(spIntent) } catch (e: Exception) {}
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search on Spotify",
                                    tint = Color(0xFF1DB954),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (index < item.missingTrackNames.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 80.dp, end = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
