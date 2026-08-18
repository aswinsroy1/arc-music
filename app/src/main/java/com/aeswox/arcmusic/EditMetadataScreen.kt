package com.aeswox.arcmusic

import android.widget.Toast
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aeswox.arcmusic.db.entities.Track
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import java.io.File
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.aeswox.arcmusic.ui.animations.jelly
import com.aeswox.arcmusic.ui.components.*

fun isFieldCorrupted(value: String?): Boolean {
    if (value.isNullOrBlank()) return true
    val lower = value.lowercase()
    if (lower == "<unknown>" || lower.startsWith("unknown")) return true
    if (value.contains("\uFFFD")) return true
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMetadataScreen(
    trackId: String,
    viewModel: MusicViewModel,
    isReadOnlyDefault: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val track = viewModel.getTrackFromLibrary(trackId)
    val context = LocalContext.current

    if (track == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Track not found")
        }
        return
    }

    var showPermissionDialog by remember { mutableStateOf(false) }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(context, "Permission denied. Cannot edit tags.", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Arc Music needs \"All files access\" permission to edit track tags and embed artworks.") },
            confirmButton = {
                JellyTextButton(onClick = {
                    showPermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = android.net.Uri.parse("package:${context.packageName}")
                            manageStorageLauncher.launch(intent)
                        } catch (e: Exception) {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            manageStorageLauncher.launch(intent)
                        }
                    }
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                JellyTextButton(onClick = { showPermissionDialog = false }) {
                    Text("Not Now")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album by remember { mutableStateOf(track.album) }
    var year by remember { mutableStateOf(track.year?.takeIf { it > 0 }?.toString() ?: "") }
    var genre by remember { mutableStateOf(track.genre ?: "") }
    var trackNumber by remember { mutableStateOf(track.trackNumber?.takeIf { it > 0 }?.toString() ?: "") }
    
    var isReadOnly by remember { mutableStateOf(isReadOnlyDefault) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.embedArtworkFromUri(track, uri)
                Toast.makeText(context, "Artwork updated from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val isAnyFieldCorrupted = remember {
        isFieldCorrupted(track.title) || isFieldCorrupted(track.artist) || isFieldCorrupted(track.album)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Track Details",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    JellyIconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isReadOnly) {
                        JellyIconButton(onClick = { isReadOnly = false }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Metadata")
                        }
                    } else {
                        JellyTextButton(
                            onClick = {
                                viewModel.updateTrackMetadata(
                                    trackId = track.id,
                                    title = title.takeIf { it.isNotBlank() },
                                    artist = artist.takeIf { it.isNotBlank() },
                                    album = album.takeIf { it.isNotBlank() },
                                    genre = genre.takeIf { it.isNotBlank() },
                                    year = year.toIntOrNull(),
                                    trackNumber = trackNumber.toIntOrNull()
                                )
                                Toast.makeText(context, "Metadata saved", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        ) {
                            Text(
                                text = "SAVE",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isAnyFieldCorrupted) {
                // Warning Box for corrupted tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFEBEE)) // Light red
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFC62828), // Dark red
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Corrupted ID3 tags detected. Please verify or update.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFC62828)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Artwork Box
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (track.artworkUri != null) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = "Album Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Artwork",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Image Action Buttons
            if (!isReadOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        onClick = { 
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Gallery", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Surface(
                        onClick = { 
                            Toast.makeText(context, "Fetching artwork...", Toast.LENGTH_SHORT).show()
                            viewModel.fetchArtworkForTrack(context, track) 
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Fetch Art",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Fetch Art", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Metadata Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Metadata",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MetadataTextField(
                            label = "Title",
                            value = title,
                            onValueChange = { title = it },
                            readOnly = isReadOnly
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        MetadataTextField(
                            label = "Artist",
                            value = artist,
                            onValueChange = { artist = it },
                            readOnly = isReadOnly
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        MetadataTextField(
                            label = "Album",
                            value = album,
                            onValueChange = { album = it },
                            readOnly = isReadOnly
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetadataTextField(
                                label = "Year",
                                value = year,
                                onValueChange = { year = it },
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number,
                                readOnly = isReadOnly
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            MetadataTextField(
                                label = "Track No.",
                                value = trackNumber,
                                onValueChange = { trackNumber = it },
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Number,
                                readOnly = isReadOnly
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        MetadataTextField(
                            label = "Genre",
                            value = genre,
                            onValueChange = { genre = it },
                            readOnly = isReadOnly
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Technical Info Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Technical Info",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val codecText = track.codec?.uppercase() ?: "UNKNOWN"
                        val bitrateKbps = ((track.bitrate ?: 0) / 1000).coerceAtLeast(0)
                        val fileSizeMB = try {
                            val file = File(track.filePath)
                            if (file.exists()) String.format("%.1f", file.length() / (1024f * 1024f)) else "0.0"
                        } catch (e: Exception) { "0.0" }
                        
                        TechnicalInfoRow(label = "Format", value = codecText)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        
                        TechnicalInfoRow(label = "Bitrate", value = "$bitrateKbps kbps")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        
                        TechnicalInfoRow(label = "File Size", value = "$fileSizeMB MB")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "File Path",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = track.filePath,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            if (!isReadOnly) {
                Spacer(modifier = Modifier.height(48.dp))
                
                JellyButton(
                    onClick = { viewModel.fetchMetadataForTrack(context, track) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Fetch Metadata",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun MetadataTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun TechnicalInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
