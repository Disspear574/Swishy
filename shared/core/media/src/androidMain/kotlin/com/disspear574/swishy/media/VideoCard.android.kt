package com.disspear574.swishy.media

import android.content.ContentUris
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
actual fun VideoCard(id: String, modifier: Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val numericId = id.toLongOrNull()

    if (numericId == null) {
        Box(modifier)
        return
    }

    val player = remember(id) {
        ExoPlayer.Builder(context).build().apply {
            val uri = ContentUris.withAppendedId(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                numericId,
            )
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(id) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = false
                isClickable = false
                isFocusable = false
                setOnTouchListener { _, _ -> false }
            }
        },
        modifier = modifier,
        update = { view -> view.player = player },
    )
}
