package com.disspear574.swishy.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PhotoCard(
    id: String,
    modifier: Modifier,
    live: Boolean = false,
    playingLive: Boolean = false,
)
