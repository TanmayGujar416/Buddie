package com.tanmay.buddie.presentation.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

class AppHapticFeedback(
    private val context: Context,
    private val composeHaptic: HapticFeedback
) {
    /**
     * Triggers a crisp, short tactile vibration when marking an item done or confirming a deletion.
     */
    fun performShortHaptic() {
        // Standard Compose tactile feedback
        try {
            composeHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
            // Ignore if device doesn't support Compose haptic
        }

        // Direct system vibration pulse (45ms) for guaranteed tactile feedback
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(45L)
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore if vibrator hardware is unavailable or in silent/DND mode
        }
    }
}

@Composable
fun rememberAppHaptics(): AppHapticFeedback {
    val context = LocalContext.current.applicationContext
    val composeHaptic = LocalHapticFeedback.current
    return remember(context, composeHaptic) {
        AppHapticFeedback(context, composeHaptic)
    }
}
