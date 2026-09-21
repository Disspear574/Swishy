package com.disspear574.swishy.media

import platform.Foundation.NSNumber
import platform.Foundation.valueForKey
import platform.Photos.PHAsset
import platform.Photos.PHAssetResource

internal fun PHAsset.fileSizeBytes(): Long {
    val resource = PHAssetResource.assetResourcesForAsset(this).firstOrNull() as? PHAssetResource
        ?: return 0
    val value = resource.valueForKey("fileSize") as? NSNumber ?: return 0
    return value.longLongValue
}
