package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = SwishyTheme.spacing

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SwishyTheme.shapes.card)
                .background(SwishyTheme.colors.surface)
                .padding(spacing.screen),
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            SwishyText(
                text = title,
                style = SwishyTheme.typography.title,
                maxLines = 2,
            )
            SwishyText(
                text = body,
                style = SwishyTheme.typography.body,
                color = SwishyTheme.colors.inkDim,
                maxLines = MAX_BODY_LINES,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = spacing.tiny),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                SwishyButton(
                    text = cancelText,
                    onClick = onDismiss,
                    tone = SwishyButtonTone.Quiet,
                    modifier = Modifier.weight(1f),
                )
                // Not colored: accent colors mean swipe directions in this app.
                SwishyButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private const val MAX_BODY_LINES = 6
