package com.aeswox.arcmusic.ui.animations

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

/**
 * Centralized navigation transition specs for Arc Music.
 *
 * Design spec: "Smooth Ease" cubic-bezier(0.22, 1, 0.36, 1) — iOS-like deceleration with
 * Material You spring physics for pop-back gestures. All animations run exclusively through
 * graphicsLayer on the RenderThread — no layout invalidation, 120fps-capable.
 *
 * Two transition families:
 *  - DETAIL: push-up (y = 25%) / pop-down for album, artist, playlist, edit screens
 *  - SHEET:  slide-up (y = 30%) / pop-down for settings, queue, utility screens
 *  - SIBLING: horizontal slide for tab-peer navigation (home ↔ library)
 */
object NavTransitions {

    // ── Easing ───────────────────────────────────────────────────────────────────

    /** iOS deceleration / Material You "Smooth Ease" */
    private val SmoothEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    /** Slightly faster variant for exits (snappier back feel) */
    private val ExitEase = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    // ── Duration ─────────────────────────────────────────────────────────────────

    private const val ENTER_DURATION = 600
    private const val EXIT_DURATION = 500
    private const val FADE_DURATION = 400

    // ── DETAIL transitions ────────────────────────────────────────────────────────
    // Used for: album_details, artist_details, playlist_details, edit_metadata,
    //           missing_*, duplicate_songs, corrupted_tags, low_quality_files

    /** Entering a detail screen: slides horizontally from right + fades in */
    val DetailEnter: EnterTransition
        get() = fadeIn(tween(FADE_DURATION)) +
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Exiting when a child pushes on top: push back (scale down) + fade */
    val DetailExit: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Returning from child via back gesture / pop: comes forward (scale up) + fade */
    val DetailPopEnter: EnterTransition
        get() = fadeIn(tween(FADE_DURATION)) +
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Being popped off the stack: slide out to the right */
    val DetailPopExit: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    // ── SHEET transitions ─────────────────────────────────────────────────────────
    // Used for: settings, appearance, jiggle_physics, equalizer, media_management,
    //           excluded_folders, collection_growth, collection_health, queue

    /** Slide horizontally from right + fade */
    val SheetEnter: EnterTransition
        get() = fadeIn(tween(FADE_DURATION)) +
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Push back (scale down) + fade */
    val SheetExit: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                scaleOut(
                    targetScale = 0.85f,
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Comes forward (scale up) + fade when popped to reveal content */
    val SheetPopEnter: EnterTransition
        get() = fadeIn(tween(FADE_DURATION)) +
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Fade-out and slide out to the right */
    val SheetPopExit: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    // ── SIBLING (horizontal) transitions ──────────────────────────────────────────
    // Used for: tab-peer routes (home ↔ library sibling screens)

    /** Navigate right: new screen slides in from right */
    val SiblingEnterFromRight: EnterTransition
        get() = fadeIn(tween(FADE_DURATION)) +
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Navigate right: old screen slides out to left */
    val SiblingExitToLeft: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Navigate left (back): new screen slides in from left */
    val SiblingEnterFromLeft: EnterTransition
        get() = fadeIn(tween(FADE_DURATION)) +
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /** Navigate left (back): old screen slides out to right */
    val SiblingExitToRight: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    // ── HOME (base screen) ────────────────────────────────────────────────────────

    /** Simple fade+scale for the root home destination entering fresh */
    val HomeEnter: EnterTransition
        get() = fadeIn(tween(FADE_DURATION)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    val HomeExit: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )

    /**
     * Returning to home via back-press: scale up from 92% with a
     * medium-bouncy spring so the home screen "pops" back into place,
     * which pairs well with the nav bar bouncing up from below.
     */
    val HomePopEnter: EnterTransition
        get() = fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )

    /** Home being popped off by a forward navigation — same as HomeExit */
    val HomePopExit: ExitTransition
        get() = fadeOut(tween(FADE_DURATION)) +
                scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(ENTER_DURATION, easing = SmoothEase)
                )
}
