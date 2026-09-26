package com.disspear574.swishy.media

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

// Photos has no link to Recently Deleted; photos-redirect:// only opens the app.
actual fun openSystemTrash() {
    val url = NSURL.URLWithString("photos-redirect://") ?: return
    UIApplication.sharedApplication.openURL(url)
}
