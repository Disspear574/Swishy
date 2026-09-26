package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun SelectableThumb(
    selected: Boolean,
    onToggle: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = SwishyTheme.colors
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(SwishyTheme.shapes.surface)
            .background(colors.surfaceSunk)
            // The whole card toggles: a corner checkbox would sit under the horizontal scroll bar.
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onToggle,
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = SELECTED_BORDER,
                        color = colors.trash,
                        shape = SwishyTheme.shapes.surface,
                    )
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
            },
    ) {
        content()

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(SwishyTheme.spacing.small)
                    .size(BADGE)
                    .clip(SwishyTheme.shapes.pill)
                    .background(colors.trash),
                contentAlignment = Alignment.Center,
            ) {
                TrashIcon(color = colors.onAccent, size = BADGE_ICON)
            }
        }
    }
}

private val SELECTED_BORDER = 3.dp
private val BADGE = 28.dp
private val BADGE_ICON = 16.dp
