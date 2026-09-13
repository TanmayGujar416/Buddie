package com.tanmay.buddie.presentation.theme

import androidx.compose.ui.graphics.Color

// Core Surfaces & Backgrounds
val CharcoalBackground = Color(0xFF121212)
val CharcoalSurface = Color(0xFF1E1E1E)
val CharcoalSurfaceElevated = Color(0xFF252525)
val CharcoalSurfaceHighlight = Color(0xFF2E2E2E)

// Brand & Blue Accents (Google Calendar inspired)
val AccentBlue = Color(0xFF1A73E8)          // Vibrant Signature Blue
val AccentBlueLight = Color(0xFF8AB4F8)     // High-contrast accessible Light Blue
val AccentBlueSubtle = Color(0xFF1A73E8).copy(alpha = 0.16f)
val AccentBlueTint = Color(0xFF1A73E8).copy(alpha = 0.04f)

// Text Hierarchy
val TextPrimary = Color(0xFFFFFFFF)         // Stark White / High Contrast
val TextSecondary = Color(0xFF9AA0A6)       // Muted Semi-transparent Grey
val TextMuted = Color(0xFF70757A)           // Subtle Hint / Caption
val TextDisabled = Color(0xFF5F6368)

// Borders & Grid Dividers
val DividerFaint = Color.White.copy(alpha = 0.08f)
val DividerSubtle = Color(0xFF2C2C2C)
val BorderOutline = Color(0xFF3C4043)
val BorderFaint = Color.White.copy(alpha = 0.12f)
val DividerVertical = Color.White.copy(alpha = 0.05f)

// Status & Semantics
val StatusError = Color(0xFFF28B82)
val StatusErrorContainer = Color(0xFF3C1414)
val StatusOnErrorContainer = Color(0xFFF6AEA9)
val StatusSuccess = Color(0xFF81C995)
val StatusWarning = Color(0xFFFDD663)
