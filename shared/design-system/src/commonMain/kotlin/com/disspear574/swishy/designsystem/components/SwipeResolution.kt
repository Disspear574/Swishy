package com.disspear574.swishy.designsystem.components

import kotlin.math.abs
import kotlin.math.min

/** Swipe outcome: right keeps, left trashes, up moves to an album. */
enum class SwipeVerdict { Keep, Trash, Move }

/** Live swipe direction and strength from 0 to 1, used for feedback during the drag. */
data class SwipeHint(val verdict: SwipeVerdict?, val magnitude: Float) {
    companion object {
        val None = SwipeHint(verdict = null, magnitude = 0f)
    }
}

fun resolveSwipe(horizontal: Float, up: Float, moveEnabled: Boolean): SwipeVerdict? {
    val lift = if (moveEnabled) up else 0f
    return when {
        // Offsets are in threshold units; up wins ties because a diagonal drag usually rises first.
        lift >= 1f && lift >= abs(horizontal) -> SwipeVerdict.Move
        horizontal >= 1f -> SwipeVerdict.Keep
        horizontal <= -1f -> SwipeVerdict.Trash
        else -> null
    }
}

fun swipeHint(horizontal: Float, up: Float, moveEnabled: Boolean): SwipeHint {
    val lift = if (moveEnabled) up.coerceAtLeast(0f) else 0f
    val side = abs(horizontal)
    return when {
        lift == 0f && side == 0f -> SwipeHint.None
        lift > side -> SwipeHint(SwipeVerdict.Move, min(lift, 1f))
        horizontal > 0f -> SwipeHint(SwipeVerdict.Keep, min(side, 1f))
        else -> SwipeHint(SwipeVerdict.Trash, min(side, 1f))
    }
}
