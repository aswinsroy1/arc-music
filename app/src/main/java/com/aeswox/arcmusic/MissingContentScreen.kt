package com.aeswox.arcmusic

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var currentTab by remember { mutableIntStateOf(0) } // 0 for Tracks, 1 for Albums
    
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
                        val itemsGrouped = if (currentTab == 0) state.missingTracks else state.missingAlbums
                        if (itemsGrouped.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No ${if (currentTab == 0) "partial" else "entire"} missing albums found.",
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
                maxLines = 1
            )
            val subText = if (item.missingCount > 0) "${item.missingCount} missing tracks" else "Full Album"
            Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Spotify Button
            FilledTonalIconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEARCH)
                    intent.setPackage("com.spotify.music")
                    val query = if (item.isAlbum) "album:${item.title} artist:${item.artistName}" else "${item.title} ${item.artistName}"
                    intent.putExtra("query", query)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback to web if app is not installed
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/${Uri.encode(query)}"))
                        try { context.startActivity(fallbackIntent) } catch (e: Exception) {}
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Spotify",
                    tint = Color(0xFF1DB954), // Spotify Green
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // YouTube Music Button
            FilledTonalIconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEARCH)
                    intent.setPackage("com.google.android.apps.youtube.music")
                    val query = if (item.isAlbum) "${item.title} album ${item.artistName}" else "${item.title} ${item.artistName}"
                    intent.putExtra("query", query)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/search?q=${Uri.encode(query)}"))
                        try { context.startActivity(fallbackIntent) } catch (e: Exception) {}
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartDisplay,
                    contentDescription = "Search YouTube Music",
                    tint = Color(0xFFFF0000), // YouTube Red
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
