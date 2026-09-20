package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun BackButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colors = SwishyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(TOUCH_SIZE)
            .alpha(if (pressed) PRESSED_ALPHA else 1f)
            .clip(SwishyTheme.shapes.pill)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.hairline, shape = SwishyTheme.shapes.pill)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(GLYPH_SIZE)) {
            val stroke = Stroke(
                width = size.minDimension * 0.14f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            )
            val path = Path().apply {
                moveTo(size.width * 0.62f, size.height * 0.20f)
                lineTo(size.width * 0.34f, size.height * 0.50f)
                lineTo(size.width * 0.62f, size.height * 0.80f)
            }
            drawPath(path = path, color = colors.ink, style = stroke)
        }
    }
}

private val TOUCH_SIZE = 44.dp
private val GLYPH_SIZE = 20.dp
private const val PRESSED_ALPHA = 0.62f
