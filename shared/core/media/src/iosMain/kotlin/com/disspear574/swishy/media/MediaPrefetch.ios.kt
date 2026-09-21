package com.disspear574.swishy.media

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Photos.PHAsset
import platform.Photos.PHCachingImageManager
import platform.Photos.PHImageContentModeAspectFit

@OptIn(ExperimentalForeignApi::class)
actual fun prefetchMedia(ids: List<String>) {
    val assets = ids.mapNotNull(::fetchAsset)
    photoManager.stopCachingImagesForAllAssets()
    if (assets.isEmpty()) return
    photoManager.startCachingImagesForAssets(
        assets = assets,
        targetSize = fullTargetSize(),
        contentMode = PHImageContentModeAspectFit,
        options = imageOptions(),
    )
}

internal val photoManager: PHCachingImageManager = PHCachingImageManager()

internal fun fetchAsset(id: String): PHAsset? =
    PHAsset.fetchAssetsWithLocalIdentifiers(listOf(id), null).firstObject as? PHAsset
