package com.disspear574.swishy.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class SwishyColors(
    val ground: Color,
    val surface: Color,
    val surfaceSunk: Color,
    val ink: Color,
    val inkDim: Color,
    val inkFaint: Color,
    val hairline: Color,
    val keep: Color,
    val trash: Color,
    val onAccent: Color,
    val isDark: Boolean,
)

internal val LightColors = SwishyColors(
    ground = Color(0xFFEFEFF2),
    surface = Color(0xFFFFFFFF),
    surfaceSunk = Color(0xFFE4E4EA),
    ink = Color(0xFF14161C),
    inkDim = Color(0xFF5B5F6B),
    inkFaint = Color(0xFF9195A1),
    hairline = Color(0xFFD6D7DE),
    keep = Color(0xFF1F6FEB),
    trash = Color(0xFFD8412F),
    onAccent = Color(0xFFFFFFFF),
    isDark = false,
)

internal val DarkColors = SwishyColors(
    ground = Color(0xFF0C0E13),
    surface = Color(0xFF161A22),
    surfaceSunk = Color(0xFF0A0C10),
    ink = Color(0xFFF2F3F6),
    inkDim = Color(0xFFA2A7B4),
    inkFaint = Color(0xFF666C7A),
    hairline = Color(0xFF262B36),
    keep = Color(0xFF4D95FF),
    trash = Color(0xFFF2604C),
    onAccent = Color(0xFF0C0E13),
    isDark = true,
)
