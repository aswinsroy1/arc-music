package com.aeswox.arcmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionHealthScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    glowIntensity: Float = 0.6f
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Collection Health", 
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                CollectionHealthScoreSection()
            }
            
            item {
                CollectionHealthBreakdownSection()
            }
            
            item {
                CollectionHealthGapsSection()
            }
            
            item {
                CollectionHealthDuplicatesCard()
            }
        }
    }
    }
}

@Composable
fun CollectionHealthScoreSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            CircularProgressIndicator(
                progress = { 0.84f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.onSurface,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "84%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 64.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "HEALTH SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Your collection is in great shape. Some metadata updates could improve your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )
    }
}

@Composable
fun CollectionHealthBreakdownSection() {
    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            HealthBreakdownItem(
                icon = Icons.Default.Warning,
                iconTint = Color(0xFFE53935),
                iconBg = Color(0xFFFFEBEE),
                title = "Missing artwork",
                subtitle = "12 songs missing artwork"
            )
            HealthBreakdownItem(
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF43A047),
                iconBg = Color(0xFFE8F5E9),
                title = "Missing lyrics",
                subtitle = "All songs have lyrics"
            )
            HealthBreakdownItem(
                icon = Icons.Default.Info,
                iconTint = Color(0xFFFDD835),
                iconBg = Color(0xFFFFF9C4),
                title = "Missing metadata",
                subtitle = "8 items with incomplete tags"
            )
            HealthBreakdownItem(
                icon = Icons.Default.FilterNone,
                iconTint = Color(0xFF1E88E5),
                iconBg = Color(0xFFE3F2FD),
                title = "Duplicate songs",
                subtitle = "3 duplicate groups found"
            )
            HealthBreakdownItem(
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF43A047),
                iconBg = Color(0xFFE8F5E9),
                title = "Corrupted tags",
                subtitle = "No corrupted tags found"
            )
            HealthBreakdownItem(
                icon = Icons.Default.Warning,
                iconTint = Color(0xFFE53935),
                iconBg = Color(0xFFFFEBEE),
                title = "Low quality files",
                subtitle = "45 tracks below 192kbps"
            )
        }
    }
}

@Composable
fun HealthBreakdownItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun CollectionHealthGapsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Requires internet",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GapCard(
                count = "14",
                label = "Missing tracks",
                subLabel = "From owned artists",
                modifier = Modifier.weight(1f)
            )
            GapCard(
                count = "3",
                label = "Missing albums",
                subLabel = "From owned artists",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GapCard(
    count: String,
    label: String,
    subLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f))
            .clickable { }
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CollectionHealthDuplicatesCard() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Refine your library",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We found 3 duplicate groups. Removing them will free up 240MB.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(
                "Review & Clean Up",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
