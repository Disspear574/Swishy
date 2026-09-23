package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun SwishyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
) {
    val colors = SwishyTheme.colors
    val spacing = SwishyTheme.spacing
    val style = SwishyTheme.typography.body.copy(color = colors.ink, fontFamily = FontFamily.Default)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MIN_HEIGHT)
            .clip(SwishyTheme.shapes.pill)
            .background(colors.surfaceSunk)
            .border(width = 1.dp, color = colors.hairline, shape = SwishyTheme.shapes.pill)
            .padding(horizontal = spacing.screen, vertical = spacing.medium),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            SwishyText(
                text = placeholder,
                style = SwishyTheme.typography.body,
                color = colors.inkFaint,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(colors.ink),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val MIN_HEIGHT = 48.dp
