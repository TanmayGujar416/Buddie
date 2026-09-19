package com.tanmay.buddie.presentation.components.mascot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Companion types supported by the Buddie companion system per docs/mascot-style-guide.md.
 */
enum class CompanionType(val displayName: String) {
    BUDDIE("Buddie"),
    MOMO("Momo"),
    WISP("Wisp");

    companion object {
        val CAT: CompanionType get() = MOMO
        val CLOUD: CompanionType get() = WISP
        val GHOST: CompanionType get() = WISP
    }
}

/**
 * Renders the currently selected companion mascot.
 *
 * Renders BuddieMascot, MomoCatMascot, or WispMascot per CompanionType.
 */
@Composable
fun SelectedCompanionMascot(
    mood: MascotMood,
    size: Dp,
    modifier: Modifier = Modifier,
    companionType: CompanionType = CompanionType.BUDDIE,
    animateIdle: Boolean = true
) {
    when (companionType) {
        CompanionType.BUDDIE -> {
            BuddieMascot(
                modifier = modifier,
                mood = mood,
                size = size,
                animateIdle = animateIdle
            )
        }
        CompanionType.MOMO -> {
            MomoCatMascot(
                modifier = modifier,
                mood = mood,
                size = size,
                animateIdle = animateIdle
            )
        }
        CompanionType.WISP -> {
            WispMascot(
                modifier = modifier,
                mood = mood,
                size = size,
                animateIdle = animateIdle
            )
        }
    }
}
