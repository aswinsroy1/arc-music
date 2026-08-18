package com.aeswox.arcmusic.ui.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

data class JigglePhysicsSettings(
    val mass: Float = 0.2f,
    val stiffness: Float = 100.0f,
    val dampingRatio: Float = 0.25f,
    val amplitudeMultiplier: Float = 1.0f,
    val gravity: Float = 9.81f
)

val LocalJigglePhysicsSettings = staticCompositionLocalOf { JigglePhysicsSettings() }

/**
 * A Physics-based overscroll modifier using a custom NestedScrollConnection.
 */
fun Modifier.physicsBounceOverscroll(
    isHorizontal: Boolean = false,
    onRefresh: (() -> Unit)? = null
): Modifier = composed {
    val jiggleSettings = LocalJigglePhysicsSettings.current
    val scope = rememberCoroutineScope()
    val translationAnim = remember { Animatable(0f) }
    
    // Physics parameters
    val m = jiggleSettings.mass
    val k = jiggleSettings.stiffness
    val zeta = jiggleSettings.dampingRatio
    val g = jiggleSettings.gravity
    val f = 2.5f

    // Precomputed Constants
    val omega0Sq = k / m
    val omega0 = sqrt(omega0Sq)
    val f0 = (omega0 / (2 * Math.PI)).toFloat()
    val A0 = (m * g) / k
    val freqRatio = f / f0
    val term1 = 1f - freqRatio * freqRatio
    val term2 = 2f * zeta * freqRatio
    val A = A0 / sqrt(term1 * term1 + term2 * term2)
    val decayRate = zeta * omega0
    val angularFrequency = (2 * Math.PI * f).toFloat()
    
    val connection = remember(scope) {
        object : NestedScrollConnection {
            var animJob: kotlinx.coroutines.Job? = null

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If the user gestures in the opposite direction of the stretch, we must consume the 
                // scroll to "unstretch" before letting the list scroll.
                val sourceStr = source.toString()
                val isGesture = !sourceStr.contains("Fling") && !sourceStr.contains("SideEffect")
                
                if (isGesture) {
                    val current = translationAnim.value
                    if (current != 0f) {
                        if (!isHorizontal && available.y != 0f && sign(current) != sign(available.y)) {
                            val proposedStretch = current + (available.y * 0.4f)
                            val (newStretch, consumedY) = if (sign(proposedStretch) != sign(current)) {
                                0f to (-current / 0.4f) // crossed 0
                            } else {
                                proposedStretch to available.y
                            }
                            animJob?.cancel()
                            animJob = scope.launch { translationAnim.snapTo(newStretch) }
                            return Offset(0f, consumedY)
                        } else if (isHorizontal && available.x != 0f && sign(current) != sign(available.x)) {
                            val proposedStretch = current + (available.x * 0.4f)
                            val (newStretch, consumedX) = if (sign(proposedStretch) != sign(current)) {
                                0f to (-current / 0.4f)
                            } else {
                                proposedStretch to available.x
                            }
                            animJob?.cancel()
                            animJob = scope.launch { translationAnim.snapTo(newStretch) }
                            return Offset(consumedX, 0f)
                        }
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val sourceStr = source.toString()
                if (sourceStr.contains("Fling") || sourceStr.contains("SideEffect")) {
                    // Do not stretch the list frame-by-frame during a fling!
                    // This aborts the fling scroll and passes the raw velocity directly to onPostFling.
                    return Offset.Zero
                }

                if (available != Offset.Zero) {
                    val maxLeftover = if (isHorizontal) available.x else available.y

                    if (abs(maxLeftover) > 0f) {
                        val current = translationAnim.value
                        // Soft resistance formula: The further we stretch, the less it gives
                        val resistance = (1f - (abs(current) / 200f)).coerceIn(0.05f, 0.4f)
                        val newStretch = current + (maxLeftover * resistance) 
                        
                        animJob?.cancel()
                        animJob = scope.launch {
                            translationAnim.snapTo(newStretch)
                        }
                        
                        return if (isHorizontal) Offset(available.x, 0f) else Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                animJob?.cancel()
                val maxLeftover = if (isHorizontal) available.x else available.y
                
                val startDisplacement = translationAnim.value
                
                if (abs(maxLeftover) > 0f || abs(startDisplacement) > 0f) {
                    if (startDisplacement > 80f && !isHorizontal) {
                        onRefresh?.invoke()
                    }
                    val isHitEdge = abs(startDisplacement) < 1f && abs(maxLeftover) > 0f
                    // Ignore small bogus velocities from lifting the finger. A real swipe is > 500f.
                    val isFling = isHitEdge || abs(maxLeftover) > 500f
                    
                    // For a fling, the momentum goes in the direction of the swipe.
                    // For a drag release, the spring force pulls back TOWARDS zero (opposite of stretch).
                    val flingDirection = if (isFling) sign(maxLeftover) else -sign(startDisplacement)
                    val intensity = if (isFling) {
                        (abs(maxLeftover) / 3000f).coerceIn(0.05f, 1.0f)
                    } else {
                        (abs(startDisplacement) / 100f).coerceIn(0.1f, 1.0f)
                    }
                    
                    val pixelScale = 3000f * jiggleSettings.amplitudeMultiplier
                    
                    val currentDecayRate = decayRate
                    
                    val durationNanos = 1200L * 1_000_000L // 1.2s
                    val startTimeNanos = withFrameNanos { it }
                    
                    try {
                        while (true) {
                            val currentNanos = withFrameNanos { it }
                            val elapsedNanos = currentNanos - startTimeNanos
                            
                            if (elapsedNanos > durationNanos) {
                                translationAnim.snapTo(0f)
                                break
                            }
                            
                            val t = elapsedNanos / 1_000_000_000f
                            val decay = exp(-currentDecayRate * t)
                            val oscillation = sin(angularFrequency * t)
                            
                            val bounceDisplacement = intensity * flingDirection * pixelScale * A * decay * oscillation
                            val returnDisplacement = startDisplacement * decay * kotlin.math.cos(angularFrequency * t)
                            
                            translationAnim.snapTo(bounceDisplacement + returnDisplacement)
                        }
                    } catch (e: Exception) {
                        // Do not snap to 0! If the user intercepts the bounce with a new touch,
                        // we must leave it at the current displacement so the drag continues smoothly.
                    }
                    
                    return if (isHorizontal) Velocity(available.x, 0f) else Velocity(0f, available.y)
                }
                return Velocity.Zero
            }
        }
    }

    this.nestedScroll(connection).graphicsLayer {
        if (isHorizontal) {
            translationX = translationAnim.value
        } else {
            translationY = translationAnim.value
        }
    }
}
