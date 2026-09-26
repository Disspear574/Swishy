package com.disspear574.swishy.media

import android.content.ContentUris
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
actual fun VideoCard(id: String, modifier: Modifier, playing: Boolean) {
    if (playing) {
        VideoPlayerCard(id = id, modifier = modifier)
    } else {
        VideoPosterCard(id = id, modifier = modifier)
    }
}

// loadThumbnail, unlike ImageDecoder, can read video frames.
@Composable
private fun VideoPosterCard(id: String, modifier: Modifier) {
    var poster by remember(id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(id) { poster = loadPoster(id) }

    PhotoFrame(bitmap = poster, modifier = modifier)
}

@Suppress("SwallowedException")
private suspend fun loadPoster(id: String): ImageBitmap? {
    val context = MediaContext.appContext ?: return null
    val numericId = id.toLongOrNull() ?: return null

    return withContext(Dispatchers.IO) {
        val uri = ContentUris.withAppendedId(
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
            numericId,
        )
        try {
            context.contentResolver
                .loadThumbnail(uri, Size(POSTER_WIDTH_PX, POSTER_HEIGHT_PX), null)
                .asImageBitmap()
        } catch (io: IOException) {
            null
        } catch (denied: SecurityException) {
            null
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerCard(id: String, modifier: Modifier) {
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

    // A leaked ExoPlayer holds a hardware decoder; after a dozen swipes video stops opening.
    DisposableEffect(id) {
        onDispose { player.release() }
    }

    AndroidView(
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                isClickable = false
                isFocusable = false
                // The view must not take the touch, or the swipe never reaches the card.
                setOnTouchListener { _, _ -> false }
            }
        },
        modifier = modifier,
        update = { view -> view.player = player },
    )
}

private const val POSTER_WIDTH_PX = 1080
private const val POSTER_HEIGHT_PX = 1920
