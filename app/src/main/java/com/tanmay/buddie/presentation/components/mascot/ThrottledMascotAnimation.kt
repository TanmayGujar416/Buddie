package com.tanmay.buddie.presentation.components.mascot

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Update interval for throttled mascot idle breathing & bobbing animation:
 * 50ms = 20 updates/sec.
 * This is visually indistinguishable from full 60/90/120Hz display refresh rates,
 * while reducing CPU/GPU wakes by ~65-80%.
 */
const val MASCOT_ANIMATION_INTERVAL_MS = 50L

/**
 * A throttled clock for mascot animations that ticks at roughly 15-20 updates/sec
 * (50ms interval) rather than relying on the platform's default frame-driven
 * InfiniteTransition timing.
 */
@Composable
fun rememberThrottledAnimationClock(
    enabled: Boolean = true,
    intervalMs: Long = MASCOT_ANIMATION_INTERVAL_MS
): State<Long> {
    val elapsedState = remember { mutableLongStateOf(0L) }

    LaunchedEffect(enabled, intervalMs) {
        if (!enabled) return@LaunchedEffect
        var lastTime = System.currentTimeMillis()
        while (isActive) {
            delay(intervalMs)
            val now = System.currentTimeMillis()
            elapsedState.longValue += (now - lastTime)
            lastTime = now
        }
    }

    return elapsedState
}

/**
 * Calculates a throttled animated value based on elapsed time, duration, and easing.
 * Supports Reverse (ping-pong) and Restart repeat modes.
 */
fun calculateThrottledValue(
    elapsedMs: Long,
    initialValue: Float,
    targetValue: Float,
    durationMs: Int,
    easing: Easing = FastOutSlowInEasing,
    repeatMode: RepeatMode = RepeatMode.Reverse
): Float {
    if (durationMs <= 0) return initialValue
    val rawFraction = when (repeatMode) {
        RepeatMode.Reverse -> {
            val cycleMs = durationMs * 2
            val position = (elapsedMs % cycleMs).toInt()
            if (position < durationMs) {
                position.toFloat() / durationMs
            } else {
                (cycleMs - position).toFloat() / durationMs
            }
        }
        RepeatMode.Restart -> {
            val position = (elapsedMs % durationMs).toInt()
            position.toFloat() / durationMs
        }
    }
    val easedFraction = easing.transform(rawFraction.coerceIn(0f, 1f))
    return initialValue + (targetValue - initialValue) * easedFraction
}
