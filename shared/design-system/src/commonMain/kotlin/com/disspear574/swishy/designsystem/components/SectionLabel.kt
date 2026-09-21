package com.disspear574.swishy.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.disspear574.swishy.designsystem.theme.SwishyTheme

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    SwishyText(
        text = text.uppercase(),
        style = SwishyTheme.typography.micro,
        color = SwishyTheme.colors.inkFaint,
        modifier = modifier.fillMaxWidth(),
    )
}
