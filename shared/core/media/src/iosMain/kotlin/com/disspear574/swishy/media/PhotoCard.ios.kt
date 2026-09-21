package com.disspear574.swishy.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSSelectorFromString
import platform.Foundation.setValue
import platform.Photos.PHAsset
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeHighQualityFormat
import platform.Photos.PHLivePhoto
import platform.Photos.PHLivePhotoRequestOptions
import platform.PhotosUI.PHLivePhotoView
import platform.PhotosUI.PHLivePhotoViewPlaybackStyleFull
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PhotoCard(
    id: String,
    modifier: Modifier,
    live: Boolean,
    playingLive: Boolean,
    preview: Boolean,
) {
    if (live && !preview) {
        LivePhotoCard(id = id, modifier = modifier, playing = playingLive)
    } else {
        StillPhotoCard(id = id, modifier = modifier, preview = preview)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun StillPhotoCard(id: String, modifier: Modifier, preview: Boolean) {
    val view = remember(id) {
        UIImageView().apply {
            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
            clipsToBounds = true
        }
    }

    LaunchedEffect(id, preview) {
        requestImage(id = id, preview = preview)?.let { image ->
            view.setImage(image)
            view.enableHighDynamicRangeIfSupported()
        }
    }

    UIKitView(
        factory = { view as UIView },
        modifier = modifier,
        properties = UIKitInteropProperties(interactionMode = null),
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun LivePhotoCard(id: String, modifier: Modifier, playing: Boolean) {
    val view = remember(id) {
        PHLivePhotoView().apply {
            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
            clipsToBounds = true
            muted = true
        }
    }

    LaunchedEffect(id) {
        view.setLivePhoto(requestLivePhoto(id))
    }

    LaunchedEffect(id, playing) {
        if (playing) {
            view.startPlaybackWithStyle(PHLivePhotoViewPlaybackStyleFull)
        } else {
            view.stopPlayback()
        }
    }

    UIKitView(
        factory = { view as UIView },
        modifier = modifier,
        properties = UIKitInteropProperties(interactionMode = null),
    )
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun requestImage(id: String, preview: Boolean): UIImage? {
    val asset = fetchAsset(id) ?: return null
    val options = PHImageRequestOptions().apply {
        deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat
        networkAccessAllowed = false
        synchronous = false
    }

    return suspendCancellableCoroutine { continuation ->
        val requestId = PHImageManager.defaultManager().requestImageForAsset(
            asset = asset,
            targetSize = if (preview) {
                CGSizeMake(PREVIEW_WIDTH, PREVIEW_HEIGHT)
            } else {
                CGSizeMake(TARGET_WIDTH, TARGET_HEIGHT)
            },
            contentMode = PHImageContentModeAspectFill,
            options = options,
        ) { result, _ -> continuation.resume(result) }
        continuation.invokeOnCancellation {
            PHImageManager.defaultManager().cancelImageRequest(requestId)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun requestLivePhoto(id: String): PHLivePhoto? {
    val asset = fetchAsset(id) ?: return null
    val options = PHLivePhotoRequestOptions().apply { networkAccessAllowed = false }

    return suspendCancellableCoroutine { continuation ->
        val requestId = PHImageManager.defaultManager().requestLivePhotoForAsset(
            asset = asset,
            targetSize = CGSizeMake(TARGET_WIDTH, TARGET_HEIGHT),
            contentMode = PHImageContentModeAspectFill,
            options = options,
        ) { result, _ -> continuation.resume(result) }
        continuation.invokeOnCancellation {
            PHImageManager.defaultManager().cancelImageRequest(requestId)
        }
    }
}

private fun fetchAsset(id: String): PHAsset? =
    PHAsset.fetchAssetsWithLocalIdentifiers(listOf(id), null).firstObject as? PHAsset

@OptIn(ExperimentalForeignApi::class)
private fun UIImageView.enableHighDynamicRangeIfSupported() {
    if (respondsToSelector(NSSelectorFromString("setPreferredImageDynamicRange:"))) {
        setValue(HIGH_DYNAMIC_RANGE, forKey = "preferredImageDynamicRange")
    }
}

/** UIImageDynamicRange.high */
private const val HIGH_DYNAMIC_RANGE = 2
private const val TARGET_WIDTH = 1080.0
private const val TARGET_HEIGHT = 1920.0

private const val PREVIEW_WIDTH = 360.0
private const val PREVIEW_HEIGHT = 640.0
