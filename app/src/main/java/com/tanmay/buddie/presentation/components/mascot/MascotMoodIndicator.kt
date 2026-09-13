package com.tanmay.buddie.presentation.components.mascot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tanmay.buddie.presentation.theme.*

/**
 * Compact, persistent mascot mood indicator widget.
 * Always visible in header bars across Home and Exams screens.
 */
@Composable
fun MascotMoodIndicator(
    mood: MascotMood,
    modifier: Modifier = Modifier
) {
    val moodLabel = when (mood) {
        MascotMood.CALM -> "Calm"
        MascotMood.AWARE -> "Aware"
        MascotMood.URGENT -> "Urgent"
        MascotMood.OVERDUE -> "Overdue"
    }

    val moodColor = when (mood) {
        MascotMood.CALM -> AccentBlueLight
        MascotMood.AWARE -> StatusWarning
        MascotMood.URGENT -> StatusError
        MascotMood.OVERDUE -> StatusError
    }

    Surface(
        modifier = modifier.semantics { contentDescription = "Current mascot mood: $moodLabel" },
        shape = RoundedCornerShape(20.dp),
        color = CharcoalSurfaceElevated,
        border = BorderStroke(1.dp, moodColor.copy(alpha = 0.40f))
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BuddieMascot(
                mood = mood,
                size = 32.dp,
                animateIdle = true
            )
            Text(
                text = moodLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = moodColor
            )
        }
    }
}
