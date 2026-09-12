package com.aeswox.arcmusic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ArcModalBottomSheet(
    currentSheet: T?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable (T) -> Unit
) {
    var activeSheet by remember { mutableStateOf<T?>(null) }
    var isContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentSheet) {
        if (currentSheet != null) {
            activeSheet = currentSheet
            isContentVisible = true
        } else {
            isContentVisible = false
            if (sheetState.isVisible) {
                sheetState.hide()
            }
            activeSheet = null
        }
    }

    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            scrimColor = Color.Black.copy(alpha = 0.4f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            },
            modifier = modifier
        ) {
            val config = LocalConfiguration.current
            
            // Jelly entrance animation inside the sliding sheet
            AnimatedVisibility(
                visible = isContentVisible,
                enter = scaleIn(
                    initialScale = 0.95f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f)
                ) + fadeIn(tween(200)),
                exit = scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(200)
                ) + fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = config.screenHeightDp.dp * 0.75f)
                ) {
                    AnimatedContent(
                        targetState = activeSheet,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300)) using
                            SizeTransform { _, _ -> tween(durationMillis = 300) }
                        },
                        label = "SheetContentAnimation"
                    ) { targetSheet ->
                        if (targetSheet != null) {
                            content(targetSheet)
                        }
                    }
                }
            }
        }
    }
}
