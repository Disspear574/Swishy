package com.disspear574.swishy.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageContentModeAspectFit
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeOpportunistic
import platform.Photos.PHImageRequestOptionsResizeModeFast
import platform.Photos.PHLivePhoto
import platform.Photos.PHLivePhotoRequestOptions
import platform.PhotosUI.PHLivePhotoView
import platform.PhotosUI.PHLivePhotoViewPlaybackStyleFull
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PhotoCard(
    id: String,
    modifier: Modifier,
    live: Boolean,
    playingLive: Boolean,
    preview: Boolean,
) {
    if (live && playingLive && !preview) {
        LivePhotoCard(id = id, modifier = modifier)
    } else {
        StillPhotoCard(id = id, modifier = modifier, preview = preview)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun StillPhotoCard(id: String, modifier: Modifier, preview: Boolean) {
    var image by remember(id) { mutableStateOf<UIImage?>(null) }
    var frame by remember(id) { mutableStateOf<ImageBitmap?>(null) }

    DisposableEffect(id, preview) {
        println("SWISHY request id=${id.take(8)} preview=$preview")
        val asset = fetchAsset(id)
        val manager = photoManager
        val requestId = asset?.let {
            manager.requestImageForAsset(
                asset = it,
                targetSize = if (preview) {
                    CGSizeMake(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                } else {
                    fullTargetSize()
                },
                contentMode = PHImageContentModeAspectFit,
                options = imageOptions(),
            ) { result, _ ->
                if (result != null && (!preview || image == null)) {
                    result.size.useContents {
                        println("SWISHY size=${width}x${height} scale=${result.scale} preview=$preview")
                    }
                    image = result
                }
            }
        }
        onDispose { requestId?.let(manager::cancelImageRequest) }
    }

    LaunchedEffect(image) {
        val source = image ?: return@LaunchedEffect
        withContext(Dispatchers.Default) { source.toImageBitmap() }?.let { frame = it }
    }

    PhotoFrame(bitmap = frame, modifier = modifier)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun LivePhotoCard(id: String, modifier: Modifier) {
    key(id) {
        var livePhoto by remember(id) { mutableStateOf<PHLivePhoto?>(null) }

        DisposableEffect(id) {
            val asset = fetchAsset(id)
            val manager = PHImageManager.defaultManager()
            val options = PHLivePhotoRequestOptions().apply { networkAccessAllowed = true }
            val requestId = asset?.let {
                manager.requestLivePhotoForAsset(
                    asset = it,
                    targetSize = CGSizeMake(TARGET_WIDTH, TARGET_HEIGHT),
                    contentMode = PHImageContentModeAspectFill,
                    options = options,
                ) { result, _ ->
                    if (result != null) {
                        livePhoto = result
                    }
                }
            }
            onDispose { requestId?.let(manager::cancelImageRequest) }
        }

        UIKitView(
            factory = {
                PHLivePhotoView().apply {
                    contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                    clipsToBounds = true
                    muted = true
                } as UIView
            },
            modifier = modifier,
            update = { view ->
                (view as? PHLivePhotoView)?.let { livePhotoView ->
                    livePhotoView.setLivePhoto(livePhoto)
                    livePhotoView.startPlaybackWithStyle(PHLivePhotoViewPlaybackStyleFull)
                }
            },
            properties = UIKitInteropProperties(interactionMode = null),
        )
    }
}

internal fun imageOptions(): PHImageRequestOptions = PHImageRequestOptions().apply {
    deliveryMode = PHImageRequestOptionsDeliveryModeOpportunistic
    resizeMode = PHImageRequestOptionsResizeModeFast
    networkAccessAllowed = true
    synchronous = false
}

@OptIn(ExperimentalForeignApi::class)
internal fun fullTargetSize(): CValue<CGSize> = CGSizeMake(TARGET_WIDTH, TARGET_HEIGHT)

private const val TARGET_WIDTH = 1080.0
private const val TARGET_HEIGHT = 1920.0

private const val PREVIEW_WIDTH = 360.0
private const val PREVIEW_HEIGHT = 640.0
