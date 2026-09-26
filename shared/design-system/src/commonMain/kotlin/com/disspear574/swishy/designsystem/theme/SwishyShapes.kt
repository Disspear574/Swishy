package com.disspear574.swishy.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class SwishyShapes(
    val card: Shape = RoundedCornerShape(26.dp),
    val surface: Shape = RoundedCornerShape(16.dp),
    val pill: Shape = RoundedCornerShape(percent = 50),
)

/** Spacing scale on a 4 dp grid; screen is the base side padding. */
@Immutable
data class SwishySpacing(
    val hair: Dp = 2.dp,
    val tiny: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val screen: Dp = 16.dp,
    val large: Dp = 24.dp,
    val huge: Dp = 32.dp,
)
