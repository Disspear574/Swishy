package com.disspear574.swishy.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
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
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun VideoCard(
    id: String,
    modifier: Modifier,
    playing: Boolean,
    loading: @Composable (progress: Float?) -> Unit,
) {
    // The poster stays under the player: it shows until the first frame and around a letterboxed one.
    Box(modifier) {
        StillPhotoCard(id = id, modifier = Modifier.matchParentSize(), preview = false)
        if (playing) {
            // The factory runs once per composition node; without key a new asset kept the old player on screen.
            key(id) {
                PlayingVideo(id = id, modifier = Modifier.matchParentSize(), loading = loading)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun PlayingVideo(id: String, modifier: Modifier, loading: @Composable (progress: Float?) -> Unit) {
    val player = remember(id) { AVPlayer() }
    val controller = remember(id) {
        AVPlayerViewController().apply {
            this.player = player
            showsPlaybackControls = false
            videoGravity = AVLayerVideoGravityResizeAspect
            view.backgroundColor = UIColor.clearColor
            // Visual Look Up would analyse every frame: CPU and memory the card has no use for.
            allowsVideoFrameAnalysis = false
        }
    }
    var progress by remember(id) { mutableStateOf<Float?>(null) }
    var ready by remember(id) { mutableStateOf(false) }
    var showLoading by remember(id) { mutableStateOf(false) }

    DisposableEffect(id) {
        // Ambient follows the Ring/Silent switch and mixes with other audio instead of stopping it.
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, null)
        AVAudioSession.sharedInstance().setActive(true, null)
        player.setMuted(false)

        var requestId: Int? = null
        val asset = PHAsset.fetchAssetsWithLocalIdentifiers(listOf(id), null).firstObject as? PHAsset
        if (asset != null) {
            val options = PHVideoRequestOptions().apply {
                // Without network access an iCloud video returns nothing and the card stays blank.
                networkAccessAllowed = true
                progressHandler = { value, _, _, _ ->
                    dispatch_async(dispatch_get_main_queue()) { progress = value.toFloat() }
                }
            }
            requestId = PHImageManager.defaultManager().requestPlayerItemForVideo(asset, options) { item, _ ->
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
            // A download left running would keep fetching a video nobody is watching.
            requestId?.let(PHImageManager.defaultManager()::cancelImageRequest)
            player.pause()
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    LaunchedEffect(id) {
        delay(LOADING_DELAY_MILLIS)
        showLoading = true
    }
    LaunchedEffect(id) {
        while (!controller.readyForDisplay) delay(READY_POLL_MILLIS)
        ready = true
    }

    Box(modifier) {
        UIKitViewController(
            factory = { controller },
            modifier = Modifier.fillMaxSize(),
            // Non-interactive so it cannot swallow the swipe; an overlay so the poster shows through.
            properties = UIKitInteropProperties(interactionMode = null, placedAsOverlay = true),
        )
        if (showLoading && !ready) {
            Box(Modifier.align(Alignment.Center)) { loading(progress) }
        }
    }
}

private const val READY_POLL_MILLIS = 50L
