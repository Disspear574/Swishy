package com.disspear574.swishy.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoCard(
    id: String,
    modifier: Modifier,
    playing: Boolean = false,
    loading: @Composable (progress: Float?) -> Unit = {},
)

// Loading shows only after this delay, so a video already on the device never flashes it.
internal const val LOADING_DELAY_MILLIS = 250L
