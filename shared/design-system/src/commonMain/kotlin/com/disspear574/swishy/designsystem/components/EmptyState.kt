package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val spacing = SwishyTheme.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.CenterVertically),
    ) {
        SwishyText(
            text = title,
            style = SwishyTheme.typography.title,
            color = SwishyTheme.colors.ink,
            maxLines = 3,
        )
        SwishyText(
            text = body,
            style = SwishyTheme.typography.body,
            color = SwishyTheme.colors.inkDim,
            maxLines = Int.MAX_VALUE,
        )
        if (actionLabel != null && onAction != null) {
            SwishyButton(text = actionLabel, onClick = onAction)
        }
    }
}
