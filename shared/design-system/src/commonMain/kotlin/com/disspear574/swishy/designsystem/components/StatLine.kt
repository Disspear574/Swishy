package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun StatLine(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
) {
    val spacing = SwishyTheme.spacing

    SwishySurface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentDescription = contentDescription,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen, vertical = spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.tiny),
        ) {
            SwishyText(
                text = value,
                style = SwishyTheme.typography.display,
                color = SwishyTheme.colors.ink,
            )
            SwishyText(
                text = caption,
                style = SwishyTheme.typography.caption,
                color = SwishyTheme.colors.inkDim,
                maxLines = 2,
            )
        }
    }
}
