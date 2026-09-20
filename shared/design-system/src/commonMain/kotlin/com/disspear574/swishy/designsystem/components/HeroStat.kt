package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun HeroStat(
    eyebrow: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val colors = SwishyTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SwishyTheme.spacing.tiny),
    ) {
        SwishyText(
            text = eyebrow.uppercase(),
            style = SwishyTheme.typography.micro,
            color = colors.inkFaint,
        )
        SwishyText(
            text = value,
            style = SwishyTheme.typography.display,
            color = colors.ink,
        )
        SwishyText(
            text = caption,
            style = SwishyTheme.typography.caption,
            color = colors.inkDim,
            maxLines = 2,
        )
    }
}
