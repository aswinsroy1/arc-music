package com.aeswox.arcmusic.sharing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.ui.animations.physicsBounceOverscroll
import com.aeswox.arcmusic.SettingsHeader
import com.aeswox.arcmusic.SettingsGroup
import com.aeswox.arcmusic.SettingsItem

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun ShareScreen(
    viewModel: ShareViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onExternalShareClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 24.dp
) {
    val sharingState by viewModel.sharingState.collectAsState()
    val discoveredEndpoints by viewModel.discoveredEndpoints.collectAsState()
    val connectionRequest by viewModel.connectionRequest.collectAsState()
    
    val transferProgress by viewModel.transferProgress.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    connectionRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.rejectConnection(request.endpointId) },
            title = { Text("Connection Request") },
            text = { Text("Accept connection to ${request.endpointName}?\n\nVerify that the PIN ${request.authCode} matches the PIN shown on the other device before accepting.") },
            confirmButton = {
                TextButton(onClick = { viewModel.acceptConnection(request.endpointId) }) { Text("Accept") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.rejectConnection(request.endpointId) }) { Text("Reject") }
            }
        )
    }

    val permissionsList = mutableListOf<String>()
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        permissionsList.addAll(listOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.NEARBY_WIFI_DEVICES,
            android.Manifest.permission.POST_NOTIFICATIONS
        ))
    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        permissionsList.addAll(listOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    } else {
        permissionsList.addAll(listOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.CHANGE_WIFI_STATE,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ))
    }

    val permissionsState = com.google.accompanist.permissions.rememberMultiplePermissionsState(permissions = permissionsList)

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    DisposableEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.startAdvertising()
            viewModel.startDiscovery()
        }
        onDispose {
            viewModel.stopAdvertising()
            viewModel.stopDiscovery()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        LazyColumn(
            contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.physicsBounceOverscroll().fillMaxSize()
        ) {
            item {
                SettingsHeader(title = "Share", fontSize = 28.sp, onNavigateBack = onNavigateBack)
            }

            item {
                // Background Glow + Icon
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .blur(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Nfc,
                            contentDescription = "Tap to Share",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Tap phones together to share instantly, or select a device below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            item {
                if (sharingState == SharingState.ERROR) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("An error occurred", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            viewModel.stopDiscovery()
                            viewModel.stopAdvertising()
                            viewModel.startAdvertising()
                            viewModel.startDiscovery()
                        }) {
                            Text("Retry")
                        }
                    }
                } else if (sharingState == SharingState.TRANSFERRING) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Transferring file...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (transferProgress > 0f) {
                            LinearProgressIndicator(progress = { transferProgress }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.cancelTransfer() }) {
                            Text("Cancel Transfer")
                        }
                    }
                } else if (sharingState == SharingState.CONNECTED) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Connected. Starting transfer...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    SettingsGroup(title = "NEARBY LISTENERS") {
                    if (discoveredEndpoints.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Searching for nearby devices...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        discoveredEndpoints.forEach { endpoint ->
                            SettingsItem(
                                icon = Icons.Outlined.Person,
                                text = endpoint.name,
                                onClick = { viewModel.requestConnection(endpoint.id) },
                                showArrow = true
                            )
                        }
                    }
                }
                }
            }

            item {
                SettingsGroup(title = "OTHER OPTIONS") {
                    SettingsItem(
                        icon = Icons.Outlined.Share,
                        text = "Share to external app",
                        onClick = { viewModel.prepareExternalShare(context) },
                        showArrow = false
                    )
                }
            }
        }
    }
}
