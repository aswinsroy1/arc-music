package com.aeswox.arcmusic

import dev.chrisbanes.haze.HazeStyle
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.MoreVert
import kotlin.math.roundToInt
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.luminance
import dev.chrisbanes.haze.HazeState
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DragHandle
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.hazeChild
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.ui.draw.blur
import androidx.compose.material3.Button
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.aeswox.arcmusic.backdrop.Backdrop
import com.aeswox.arcmusic.backdrop.drawBackdrop
import com.aeswox.arcmusic.backdrop.effects.blur
import com.aeswox.arcmusic.backdrop.backdrops.layerBackdrop
import dev.chrisbanes.haze.haze
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.composed
import androidx.compose.foundation.shape.CornerBasedShape

import androidx.compose.ui.platform.LocalContext
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette

@Composable
fun rememberDominantColor(imageUrl: String?, defaultColor: Color): State<Color> {
    val context = LocalContext.current
    val colorState = remember { mutableStateOf(defaultColor) }

    LaunchedEffect(imageUrl) {
        if (imageUrl == null) {
            colorState.value = defaultColor
            return@LaunchedEffect
        }
        
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(128)
            .allowHardware(false)
            .build()
            
        val result = context.imageLoader.execute(request)
        if (result is SuccessResult) {
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                Palette.from(bitmap).generate { palette ->
                    palette?.dominantSwatch?.rgb?.let { color ->
                        colorState.value = Color(color)
                    } ?: palette?.mutedSwatch?.rgb?.let { color ->
                        colorState.value = Color(color)
                    }
                }
            }
        } else {
            colorState.value = defaultColor
        }
    }
    
    return colorState
}

val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }



val AppCornerRadius = 32.dp

fun Modifier.applyHazeAndBackdrop(hazeState: HazeState?): Modifier = composed {
    var modifier = this
    if (hazeState != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        modifier = modifier.haze(state = hazeState)
    } else if (android.os.Build.VERSION.SDK_INT in 31..33) {
        val backdrop = LocalAppBackdrop.current
        if (backdrop is com.aeswox.arcmusic.backdrop.backdrops.LayerBackdrop) {
            modifier = modifier.layerBackdrop(backdrop)
        }
    }
    modifier
}

fun Modifier.glassEffect(
    hazeState: HazeState?,
    tintTransparency: Float,
    noiseFactor: Float,
    shape: Shape = RoundedCornerShape(AppCornerRadius),
    forceFallback: Boolean = false
): Modifier = composed {
    // Detect dark mode from the actual applied color scheme (luminance < 0.05 = dark background).
    val bgLuminance = MaterialTheme.colorScheme.background.luminance()
    val isDark = bgLuminance < 0.05f
    val tintBase = if (isDark) Color.Black else Color.White
    // Dark mode uses slightly higher alpha to keep the glass visible against black.
    val adjustedAlpha = if (isDark) (tintTransparency + 0.3f).coerceAtMost(0.85f) else tintTransparency

    if (!forceFallback && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        if (hazeState != null) {
            this.hazeChild(
                state = hazeState,
                shape = shape,
                style = HazeStyle(
                    blurRadius = 24.dp,
                    tint = tintBase.copy(alpha = adjustedAlpha),
                    noiseFactor = noiseFactor
                )
            )
        } else {
            this.background(tintBase.copy(alpha = adjustedAlpha), shape)
        }
    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val backdrop = LocalAppBackdrop.current
        if (backdrop != null && shape is CornerBasedShape) {
            val density = LocalDensity.current
            val blurPx = with(density) { 24.dp.toPx() } * 0.5f // scale factor
            
            this.drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = { blur(blurPx) },
                onDrawSurface = { drawRect(tintBase.copy(alpha = adjustedAlpha)) },
                backdropScale = 0.5f
            )
        } else {
            this.background(tintBase.copy(alpha = adjustedAlpha + 0.3f), shape)
        }
    } else {
        // Fallback to semi-transparent background for Android 11 and below
        this.background(tintBase.copy(alpha = adjustedAlpha + 0.3f), shape)
    }
}

@Composable
fun AnimatedGlowBackground(modifier: Modifier = Modifier, glowIntensity: Float, color: Color = Color(0xFF5E90A7)) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(durationMillis = 1000)
    )
    val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = glowIntensity * 0.75f,
        targetValue = glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val animatedOffsetX by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowOffsetX"
    )
    val animatedOffsetY by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowOffsetY"
    )
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = animatedOffsetX.dp, y = animatedOffsetY.dp)
                .size(600.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = animatedAlpha),
                            animatedColor.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier, 
    title: String = "Care", 
    artist: String = "Conan Gray", 
    imageUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuDcxr5OkSQfpI_jTkSInZTLTIQNPElvx4VTAwf6InyR5cV2DD4SLzOgYsBC1gNArokFiZMFSwmKVi6VW-OeV6ouanmXDcfN4aD-RtGJFuMNyYZTx5P6VkXi-b4eY5GWUNpAaGeTkiqgkdzS6Of-mtUzJt7rz9IYbGhj7V3IcTi8iHjlof7t5fJzN09WsP72jlTq2o-VEsgIRAPXzreisxiQKK8kmsYEbFlDl442gyzxMfa0UGT2M3aJ5eafCHY0tM_wkFed6Lty8vDU", 
    hazeState: HazeState? = null, 
    tintTransparency: Float = 0.4f, 
    noiseFactor: Float = 0.06f,
    isPlaying: Boolean = false,
    onPlayPauseClick: () -> Unit = {},
    onSkipNextClick: () -> Unit = {},
    onClick: () -> Unit = {}, 
    onDismiss: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetY.value > 150f) {
                                onDismiss()
                                offsetY.snapTo(0f)
                            } else {
                                offsetY.animateTo(0f)
                            }
                        }
                    },
                    onDragCancel = { 
                        coroutineScope.launch { offsetY.animateTo(0f) }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    if (offsetY.value + dragAmount > 0) {
                        coroutineScope.launch { offsetY.snapTo(offsetY.value + dragAmount) }
                    }
                }
            }
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(AppCornerRadius))
            .glassEffect(hazeState, tintTransparency, noiseFactor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), 
                color = MaterialTheme.colorScheme.onSurface, 
                maxLines = 1
            )
            Text(
                text = artist, 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                maxLines = 1
            )
        }
        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, 
                contentDescription = if (isPlaying) "Pause" else "Play", 
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onSkipNextClick) {
            Icon(
                imageVector = Icons.Filled.SkipNext, 
                contentDescription = "Skip Next", 
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BottomNavigation(
    currentTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier, 
    hazeState: HazeState? = null, 
    tintTransparency: Float = 0.4f, 
    noiseFactor: Float = 0.06f
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(AppCornerRadius))
            .glassEffect(hazeState, tintTransparency, noiseFactor)
            .padding(horizontal = 24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentTab == 0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .clickable { onTabSelected(0) }
        ) {
            Icon(
                imageVector = Icons.Default.Home, 
                contentDescription = "Home", 
                tint = if (currentTab == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentTab == 1) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .clickable { onTabSelected(1) }
        ) {
            Icon(
                imageVector = Icons.Default.Search, 
                contentDescription = "Search", 
                tint = if (currentTab == 1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (currentTab == 2) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .clickable { onTabSelected(2) }
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic, 
                contentDescription = "Library", 
                tint = if (currentTab == 2) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(50),
        contentPadding = contentPadding,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun DialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    )
}

@Composable
fun ReorderableDragHandle(
    modifier: Modifier = Modifier,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onVerticalDrag: (change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Float) -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Icon(
        imageVector = Icons.Default.DragHandle, 
        contentDescription = "Reorder",
        tint = tint,
        modifier = modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = { _ -> onDragStart() },
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onVerticalDrag = onVerticalDrag
            )
        }
    )
}

@Composable
fun AppIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    size: androidx.compose.ui.unit.Dp = 24.dp
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun ArcDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(AppCornerRadius),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(AppCornerRadius))
    ) {
        content()
    }
}

@Composable
fun ArcDropdownMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val iconTint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    
    DropdownMenuItem(
        text = { 
            Text(text, style = MaterialTheme.typography.bodyMedium, color = color) 
        },
        onClick = onClick,
        leadingIcon = { Icon(icon, contentDescription = null, tint = iconTint) }
    )
}
