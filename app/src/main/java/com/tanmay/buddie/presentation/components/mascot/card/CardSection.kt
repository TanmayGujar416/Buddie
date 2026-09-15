package com.tanmay.buddie.presentation.components.mascot.card

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class CardSection(
    val title: String,
    val icon: ImageVector
) {
    SUBMISSIONS("Assignments", Icons.Default.Home),
    TIMETABLE("Timetable", Icons.Default.DateRange),
    EXAMS("Exams", Icons.Default.Edit)
}
