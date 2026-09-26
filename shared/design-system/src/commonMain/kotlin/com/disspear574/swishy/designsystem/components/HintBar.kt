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
    moveHint: String? = null,
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
        // Shown only where the platform supports albums; a hint for a dead gesture is worse than none.
        if (moveHint != null) {
            SwishyText(
                text = moveHint,
                style = SwishyTheme.typography.micro,
                color = colors.move,
            )
        }
        SwishyText(
            text = keepHint,
            style = SwishyTheme.typography.micro,
            color = colors.keep,
        )
    }
}
