package com.aeswox.arcmusic.ui.animations

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.widget.NestedScrollView
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A physics-based bounce effect modeled as a forced damped harmonic oscillator.
 * Centralized to be used across different parts of the UI.
 */
class PhysicsBounceAnimator(
    private val view: View,
    private val pixelScale: Float = 3000f // Scale from physics units (meters) to screen pixels
) {
    // Given Physics Parameters
    private val m = 0.2f
    private val k = 100.0f
    private val zeta = 0.25f // ζ
    private val g = 9.81f
    private val f = 2.5f

    // Precomputed Constants
    private val omega0: Float
    private val A: Float
    private val decayRate: Float
    private val angularFrequency: Float

    init {
        val omega0Sq = k / m
        omega0 = sqrt(omega0Sq)
        val f0 = (omega0 / (2 * Math.PI)).toFloat()
        val A0 = (m * g) / k

        val freqRatio = f / f0
        val term1 = 1f - freqRatio * freqRatio
        val term2 = 2f * zeta * freqRatio
        
        A = A0 / sqrt(term1 * term1 + term2 * term2)
        
        decayRate = zeta * omega0
        angularFrequency = (2 * Math.PI * f).toFloat()
    }

    private var animator: ValueAnimator? = null

    /**
     * Starts the bounce effect on the target view.
     * 
     * @param intensity Multiplier for the bounce amplitude (based on scroll velocity).
     * @param direction 1f for overscrolling top (pulling down), -1f for overscrolling bottom (pulling up).
     */
    fun startBounce(intensity: Float = 1.0f, direction: Float = 1.0f) {
        animator?.cancel()

        // 1.2 seconds duration covers exactly 3 cycles at 2.5 Hz (3 / 2.5 = 1.2)
        val durationMs = 1200L 
        
        animator = ValueAnimator.ofFloat(0f, durationMs / 1000f).apply {
            duration = durationMs
            interpolator = LinearInterpolator() // We calculate our own easing
            
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                
                // Equation: x(t) = A * e^(-ζω₀t) * sin(2πft)
                val decay = exp(-decayRate * t)
                val oscillation = sin(angularFrequency * t)
                
                val displacement = intensity * direction * pixelScale * A * decay * oscillation
                
                view.translationY = displacement
            }
            
            start()
        }
    }
    
    fun cancel() {
        animator?.cancel()
        view.translationY = 0f
    }
}

/**
 * Integration Example: Attach to NestedScrollView
 */
fun NestedScrollView.setupPhysicsBounceOverscroll() {
    val animator = PhysicsBounceAnimator(this)
    
    this.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
        val maxScroll = this.getChildAt(0).height - this.height
        
        // Detect overscroll attempts at bounds
        if (scrollY == 0 && oldScrollY > 0) {
            // Hit top
            val velocityApproximation = oldScrollY.toFloat() // Simplified velocity
            animator.startBounce(intensity = velocityApproximation.coerceAtMost(2f), direction = 1f)
        } else if (scrollY == maxScroll && oldScrollY < maxScroll) {
            // Hit bottom
            val velocityApproximation = (maxScroll - oldScrollY).toFloat()
            animator.startBounce(intensity = velocityApproximation.coerceAtMost(2f), direction = -1f)
        }
    }
}
