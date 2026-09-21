package com.disspear574.swishy.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSSelectorFromString
import platform.Foundation.setValue
import platform.Photos.PHAsset
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageContentModeAspectFit
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHImageRequestOptionsDeliveryModeOpportunistic
import platform.Photos.PHLivePhoto
import platform.Photos.PHLivePhotoRequestOptions
import platform.PhotosUI.PHLivePhotoView
import platform.PhotosUI.PHLivePhotoViewPlaybackStyleFull
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.UIKit.UIVisualEffectView

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
    var image by remember(id) { mutableStateOf<UIImage?>(null) }

    DisposableEffect(id, preview) {
        val asset = fetchAsset(id)
        val manager = PHImageManager.defaultManager()
        val requestId = asset?.let {
            manager.requestImageForAsset(
                asset = it,
                targetSize = if (preview) {
                    CGSizeMake(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                } else {
                    CGSizeMake(TARGET_WIDTH, TARGET_HEIGHT)
                },
                contentMode = PHImageContentModeAspectFit,
                options = imageOptions(),
            ) { result, _ ->
                if (result != null && (!preview || image == null)) {
                    image = result
                }
            }
        }
        onDispose { requestId?.let(manager::cancelImageRequest) }
    }

    UIKitView(
        factory = { PhotoCardView() as UIView },
        modifier = modifier,
        update = { view -> (view as? PhotoCardView)?.show(image) },
        properties = UIKitInteropProperties(interactionMode = null),
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PhotoCardView : UIView(frame = CGRectZero.readValue()) {
    private val backdrop = UIImageView().apply {
        contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
        clipsToBounds = true
        alpha = BACKDROP_ALPHA
    }
    private val blur = UIVisualEffectView(
        effect = UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleSystemMaterialDark),
    )
    private val front = UIImageView().apply {
        contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
        clipsToBounds = true
    }

    init {
        clipsToBounds = true
        listOf(backdrop, blur, front).forEach { subview ->
            addSubview(subview)
            subview.pinToEdgesOf(this)
        }
    }

    fun show(image: UIImage?) {
        backdrop.setImage(image)
        front.setImage(image)
        front.enableHighDynamicRangeIfSupported()
    }
}

private fun UIView.pinToEdgesOf(parent: UIView) {
    translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activateConstraints(
        listOf(
            leadingAnchor.constraintEqualToAnchor(parent.leadingAnchor),
            trailingAnchor.constraintEqualToAnchor(parent.trailingAnchor),
            topAnchor.constraintEqualToAnchor(parent.topAnchor),
            bottomAnchor.constraintEqualToAnchor(parent.bottomAnchor),
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun LivePhotoCard(id: String, modifier: Modifier, playing: Boolean) {
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
                    contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
                    clipsToBounds = true
                    muted = true
                } as UIView
            },
            modifier = modifier,
            update = { view ->
                (view as? PHLivePhotoView)?.let { livePhotoView ->
                    livePhotoView.setLivePhoto(livePhoto)
                    if (playing) {
                        livePhotoView.startPlaybackWithStyle(PHLivePhotoViewPlaybackStyleFull)
                    } else {
                        livePhotoView.stopPlayback()
                    }
                }
            },
            properties = UIKitInteropProperties(interactionMode = null),
        )
    }
}

private fun imageOptions(): PHImageRequestOptions = PHImageRequestOptions().apply {
    deliveryMode = PHImageRequestOptionsDeliveryModeOpportunistic
    networkAccessAllowed = true
    synchronous = false
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

private const val BACKDROP_ALPHA = 0.5

private const val PREVIEW_WIDTH = 360.0
private const val PREVIEW_HEIGHT = 640.0
