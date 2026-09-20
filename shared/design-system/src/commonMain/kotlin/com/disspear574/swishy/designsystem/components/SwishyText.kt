package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun SwishyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SwishyTheme.typography.body,
    color: Color = SwishyTheme.colors.ink,
    maxLines: Int = 1,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color, fontFamily = FontFamily.Default),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
