package com.aeswox.arcmusic.sharing

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeswox.arcmusic.R
import com.aeswox.arcmusic.ui.components.JellyIconButton

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun ReceiveScreen(
    viewModel: ShareViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val connectionRequest by viewModel.connectionRequest.collectAsState()
    val sharingState by viewModel.sharingState.collectAsState()
    
    connectionRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { viewModel.rejectConnection(request.endpointId) },
            title = { Text("Connection Request") },
            text = { Text("Accept connection from ${request.endpointName}?\n\nAuthentication PIN: ${request.authCode}") },
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
            android.Manifest.permission.NEARBY_WIFI_DEVICES
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
        }
        onDispose {
            viewModel.stopAdvertising()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JellyIconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Receive Music",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 48.dp), // Balance the back button
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Concentric Circles & Icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer circle
                val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                val secondaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                Canvas(modifier = Modifier.fillMaxSize().scale(scale)) {
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2 - 10.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = secondaryColor,
                        radius = size.minDimension / 3,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = surfaceVariantColor,
                        radius = size.minDimension / 4,
                        style = androidx.compose.ui.graphics.drawscope.Fill
                    )
                }

                // App Icon Box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Arc Music",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Searching Pill
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            if (sharingState == SharingState.TRANSFERRING) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Receiving file...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                }
            } else if (sharingState == SharingState.CONNECTED) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Connected. Starting transfer...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Searching",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp).rotate(rotation)
                    )
                    Text(
                        text = "SEARCHING...",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Text
            Text(
                text = "Ready to receive. Bring devices close to\ninstantly transfer music.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
