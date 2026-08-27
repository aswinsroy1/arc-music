package com.aeswox.arcmusic.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aeswox.arcmusic.MusicViewModel
import com.aeswox.arcmusic.R
import com.aeswox.arcmusic.ThemeMode
import com.aeswox.arcmusic.ui.animations.NavTransitions
import com.aeswox.arcmusic.ui.animations.jellyClick
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    
    val themeMode by viewModel.themeMode.collectAsState()
    val availableFolders by viewModel.availableAudioFolders.collectAsState()
    var checkedFolders by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Automatically select all folders initially when they load
    LaunchedEffect(availableFolders) {
        if (availableFolders.isNotEmpty() && checkedFolders.isEmpty()) {
            checkedFolders = availableFolders.toSet()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Dot indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val isSelected = currentPage == index
                    val width by animateFloatAsState(targetValue = if (isSelected) 24f else 8f, label = "dotWidth")
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    NavTransitions.DetailEnter togetherWith NavTransitions.DetailPopExit
                } else {
                    NavTransitions.DetailPopEnter togetherWith NavTransitions.DetailExit
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> WelcomePage(
                    onNext = { currentPage = 1 }
                )
                1 -> ThemeSelectionPage(
                    currentTheme = themeMode,
                    onThemeSelect = { viewModel.setThemeMode(it) },
                    onNext = { currentPage = 2 }
                )
                2 -> PermissionsPage(
                    onNext = { 
                        viewModel.loadAvailableAudioFolders()
                        currentPage = 3 
                    }
                )
                3 -> FolderSelectionPage(
                    availableFolders = availableFolders,
                    checkedFolders = checkedFolders,
                    onToggleFolder = { folder ->
                        checkedFolders = if (checkedFolders.contains(folder)) {
                            checkedFolders - folder
                        } else {
                            checkedFolders + folder
                        }
                    },
                    onToggleAll = {
                        checkedFolders = if (checkedFolders.size == availableFolders.size) {
                            emptySet()
                        } else {
                            availableFolders.toSet()
                        }
                    },
                    onScanClick = {
                        val newExcluded = availableFolders.filter { !checkedFolders.contains(it) }
                        viewModel.setExcludedFolders(newExcluded)
                        viewModel.scanMediaStore()
                        currentPage = 4
                    }
                )
                4 -> LibraryScanningPage(
                    viewModel = viewModel,
                    onFinish = {
                        viewModel.setHasCompletedOnboarding(true)
                        onFinish()
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to\nArc Music",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your offline music experience.\nBeautifully simple.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Image(
            painter = painterResource(id = R.drawable.onboarding_hero),
            contentDescription = "Welcome illustration",
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(32.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
                .jellyClick(onClick = onNext),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Get started", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun ThemeSelectionPage(
    currentTheme: ThemeMode,
    onThemeSelect: (ThemeMode) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose your theme",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You can change this later in settings.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        ThemeOptionCard(
            title = "Light",
            isSelected = currentTheme == ThemeMode.Light,
            isDarkOption = false,
            onClick = { onThemeSelect(ThemeMode.Light) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ThemeOptionCard(
            title = "Dark",
            isSelected = currentTheme == ThemeMode.Dark,
            isDarkOption = true,
            onClick = { onThemeSelect(ThemeMode.Dark) }
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .jellyClick(onClick = onNext),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun ThemeOptionCard(
    title: String,
    isSelected: Boolean,
    isDarkOption: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isDarkOption) Color(0xFF1E1E24) else Color(0xFFF3F3F5)
    val textColor = if (isDarkOption) Color.White else Color.Black
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(80.dp)
            .jellyClick(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDarkOption) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = textColor
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsPage(onNext: () -> Unit) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    val permissionState = rememberPermissionState(permission)
    
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Grant permission",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We need access to your files to find\nyour music.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        // Placeholder for the folder image
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (permissionState.status.isGranted) {
                    onNext()
                } else {
                    permissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .jellyClick(onClick = {
                    if (permissionState.status.isGranted) {
                        onNext()
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                }),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (permissionState.status.isGranted) "Granted! Continue" else "Grant access", 
                 fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun FolderSelectionPage(
    availableFolders: List<String>,
    checkedFolders: Set<String>,
    onToggleFolder: (String) -> Unit,
    onToggleAll: () -> Unit,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Folders",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onToggleAll) {
                Text(
                    text = if (checkedFolders.size == availableFolders.size) "DESELECT ALL" else "SELECT ALL",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose the folders where you store your music. You can always change this later in settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(availableFolders) { folder ->
                val isChecked = checkedFolders.contains(folder)
                Surface(
                    onClick = { onToggleFolder(folder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .jellyClick(onClick = { onToggleFolder(folder) }),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            // Extract just the folder name instead of full path
                            val folderName = folder.substringAfterLast("/")
                            val parentPath = folder.substringBeforeLast("/", "")
                            Text(
                                text = folderName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (parentPath.isNotBlank()) {
                                Text(
                                    text = parentPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = null // handled by Surface click
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp)
                .jellyClick(onClick = onScanClick),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Scan Selected Folders", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun LibraryScanningPage(
    viewModel: MusicViewModel,
    onFinish: () -> Unit
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    
    // Automatically finish when scanning completes after having started
    var hasStartedScanning by remember { mutableStateOf(false) }
    
    LaunchedEffect(isScanning) {
        if (isScanning) {
            hasStartedScanning = true
        } else if (hasStartedScanning && !isScanning) {
            // Give a little delay for UX feeling
            kotlinx.coroutines.delay(800)
            onFinish()
        }
    }
    
    // Fallback if scanning doesn't start properly or finishes instantly
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000)
        if (!isScanning && !hasStartedScanning) {
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Scanning your library",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This may take a few minutes depending on the size of your library.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(64.dp))
        
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 
                    if (scanProgress.isCompleted) 1f 
                    else if (scanProgress.total > 0) scanProgress.current.toFloat() / scanProgress.total.toFloat() 
                    else 0f 
                },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Text(
                text = if (scanProgress.isCompleted) "100%" 
                else if (scanProgress.total > 0) {
                    val pct = (scanProgress.current.toFloat() / scanProgress.total * 100).toInt()
                    "$pct%"
                } else "0%",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .jellyClick(onClick = onFinish),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                "Run in Background",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
