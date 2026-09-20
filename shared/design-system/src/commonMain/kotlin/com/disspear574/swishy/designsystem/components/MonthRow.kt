package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun MonthRow(
    title: String,
    subtitle: String,
    size: String,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    done: Boolean = false,
) {
    val colors = SwishyTheme.colors
    val spacing = SwishyTheme.spacing

    SwishySurface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentDescription = contentDescription,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screen, vertical = spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.tiny),
            ) {
                SwishyText(
                    text = title,
                    style = SwishyTheme.typography.label,
                    color = if (done) colors.inkFaint else colors.ink,
                )
                SwishyText(
                    text = subtitle,
                    style = SwishyTheme.typography.caption,
                    color = colors.inkDim,
                )
            }
            Spacer(Modifier.width(spacing.medium))
            SwishyText(
                text = size,
                style = SwishyTheme.typography.numeric,
                color = if (done) colors.inkFaint else colors.inkDim,
            )
        }
    }
}
