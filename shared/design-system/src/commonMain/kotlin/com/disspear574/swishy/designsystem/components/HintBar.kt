package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun HintBar(
    trashHint: String,
    keepHint: String,
    modifier: Modifier = Modifier,
) {
    val colors = SwishyTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SwishyText(
            text = trashHint,
            style = SwishyTheme.typography.micro,
            color = colors.trash,
        )
        SwishyText(
            text = keepHint,
            style = SwishyTheme.typography.micro,
            color = colors.keep,
        )
    }
}
