package com.disspear574.swishy.media

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openSystemTrash() {
    val url = NSURL.URLWithString("photos-redirect://") ?: return
    UIApplication.sharedApplication.openURL(url)
}
