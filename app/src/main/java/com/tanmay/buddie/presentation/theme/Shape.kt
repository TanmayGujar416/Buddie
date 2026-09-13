package com.tanmay.buddie.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Squircle & Specific Element Shapes
val SquircleShape = RoundedCornerShape(16.dp)   // Standard squircle for FABs, prominent cards, dialogs
val CardShape = RoundedCornerShape(16.dp)
val DialogShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val InputFieldShape = RoundedCornerShape(12.dp)
val ChipShape = RoundedCornerShape(8.dp)
val EventBlockShape = RoundedCornerShape(6.dp)

val BuddieShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
