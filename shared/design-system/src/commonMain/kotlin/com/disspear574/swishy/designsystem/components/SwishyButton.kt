package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme

enum class SwishyButtonTone {
    Solid,
    Quiet,
}

@Composable
fun SwishyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: SwishyButtonTone = SwishyButtonTone.Solid,
    enabled: Boolean = true,
) {
    val colors = SwishyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val background = when (tone) {
        SwishyButtonTone.Solid -> colors.ink
        SwishyButtonTone.Quiet -> colors.surface
    }
    val content = when (tone) {
        SwishyButtonTone.Solid -> colors.ground
        SwishyButtonTone.Quiet -> colors.ink
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .alpha(
                when {
                    !enabled -> DISABLED_ALPHA
                    pressed -> PRESSED_ALPHA
                    else -> 1f
                },
            )
            .clip(SwishyTheme.shapes.pill)
            .background(background)
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp, vertical = 13.dp)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        SwishyText(
            text = text,
            style = SwishyTheme.typography.label,
            color = content,
            maxLines = 2,
        )
    }
}

private const val PRESSED_ALPHA = 0.62f
private const val DISABLED_ALPHA = 0.38f
