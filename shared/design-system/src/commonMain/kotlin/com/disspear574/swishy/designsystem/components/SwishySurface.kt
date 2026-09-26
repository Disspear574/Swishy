package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun SwishySurface(
    modifier: Modifier = Modifier,
    shape: Shape = SwishyTheme.shapes.surface,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = SwishyTheme.colors
    val clickable = if (onClick != null) {
        Modifier
            // Merged so a screen reader announces one labeled, clickable element.
            .semantics(mergeDescendants = true) {
                if (contentDescription != null) this.contentDescription = contentDescription
            }
            .clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.hairline, shape = shape)
            .then(clickable),
    ) {
        content()
    }
}
