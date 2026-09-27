package com.disspear574.swishy.media

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setMuted
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Photos.PHAsset
import platform.Photos.PHImageManager
import platform.Photos.PHVideoRequestOptions
import platform.UIKit.UIColor

@Composable
actual fun VideoCard(id: String, modifier: Modifier, playing: Boolean) {
    // The poster stays under the player: it shows until the first frame and around a letterboxed one.
    Box(modifier) {
        StillPhotoCard(id = id, modifier = Modifier.matchParentSize(), preview = false)
        if (playing) {
            // The factory runs once per composition node; without key a new asset kept the old player on screen.
            key(id) {
                VideoCardContent(id = id, modifier = Modifier.matchParentSize())
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun VideoCardContent(id: String, modifier: Modifier) {
    val player = remember(id) { AVPlayer() }

    DisposableEffect(id) {
        player.setMuted(true)

        val asset = PHAsset.fetchAssetsWithLocalIdentifiers(listOf(id), null)
            .firstObject as? PHAsset
        if (asset != null) {
            // Without network access an iCloud video returns nothing and the card stays black.
            val options = PHVideoRequestOptions().apply { networkAccessAllowed = true }
            PHImageManager.defaultManager().requestPlayerItemForVideo(
                asset = asset,
                options = options,
            ) { item, _ ->
                if (item != null) {
                    player.replaceCurrentItemWithPlayerItem(item)
                    player.play()
                }
            }
        }

        // AVPlayer does not loop by itself; a stopped video looks like a frozen photo.
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = null,
        ) { _ ->
            player.seekToTime(CMTimeMake(value = 0, timescale = 1))
            player.play()
        }

        onDispose {
            player.pause()
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    // A controller lays itself out; an AVPlayerLayer would need manual resizing without onResize.
    UIKitViewController(
        factory = {
            AVPlayerViewController().apply {
                this.player = player
                showsPlaybackControls = false
                videoGravity = AVLayerVideoGravityResizeAspect
                view.backgroundColor = UIColor.clearColor
            }
        },
        modifier = modifier,
        // Non-interactive so it cannot swallow the swipe; an overlay so the poster shows through.
        properties = UIKitInteropProperties(interactionMode = null, placedAsOverlay = true),
    )
}
