package com.disspear574.swishy.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.disspear574.swishy.designsystem.resources.Res
import com.disspear574.swishy.designsystem.resources.onest_bold
import com.disspear574.swishy.designsystem.resources.onest_medium
import com.disspear574.swishy.designsystem.resources.onest_regular
import com.disspear574.swishy.designsystem.resources.onest_semibold
import com.disspear574.swishy.designsystem.resources.plex_mono_medium
import org.jetbrains.compose.resources.Font

@Immutable
data class SwishyTypography(
    val display: TextStyle,
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val numeric: TextStyle,
    val micro: TextStyle,
)

@Composable
internal fun onestFamily(): FontFamily = FontFamily(
    Font(Res.font.onest_regular, FontWeight.Normal),
    Font(Res.font.onest_medium, FontWeight.Medium),
    Font(Res.font.onest_semibold, FontWeight.SemiBold),
    Font(Res.font.onest_bold, FontWeight.Bold),
)

@Composable
internal fun monoFamily(): FontFamily = FontFamily(
    Font(Res.font.plex_mono_medium, FontWeight.Medium),
)

@Composable
internal fun swishyTypography(): SwishyTypography {
    val sans = onestFamily()
    val mono = monoFamily()

    return SwishyTypography(
        display = TextStyle(
            fontFamily = sans,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.03).em,
        ),
        title = TextStyle(
            fontFamily = sans,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).em,
        ),
        body = TextStyle(
            fontFamily = sans,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
        ),
        label = TextStyle(
            fontFamily = sans,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.01).em,
        ),
        caption = TextStyle(
            fontFamily = sans,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
        ),
        numeric = TextStyle(
            fontFamily = mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        ),
        micro = TextStyle(
            fontFamily = mono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.14.em,
        ),
    )
}

internal val DefaultOverflow = TextOverflow.Ellipsis
