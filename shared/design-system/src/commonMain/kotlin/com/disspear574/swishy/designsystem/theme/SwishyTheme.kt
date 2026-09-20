package com.disspear574.swishy.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalSwishyColors = staticCompositionLocalOf { LightColors }
private val LocalSwishyTypography = staticCompositionLocalOf { swishyTypography() }
private val LocalSwishyShapes = staticCompositionLocalOf { SwishyShapes() }
private val LocalSwishySpacing = staticCompositionLocalOf { SwishySpacing() }

@Composable
fun SwishyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSwishyColors provides if (darkTheme) DarkColors else LightColors,
        LocalSwishyTypography provides swishyTypography(),
        LocalSwishyShapes provides SwishyShapes(),
        LocalSwishySpacing provides SwishySpacing(),
        content = content,
    )
}

object SwishyTheme {

    val colors: SwishyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSwishyColors.current

    val typography: SwishyTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalSwishyTypography.current

    val shapes: SwishyShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalSwishyShapes.current

    val spacing: SwishySpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSwishySpacing.current
}
