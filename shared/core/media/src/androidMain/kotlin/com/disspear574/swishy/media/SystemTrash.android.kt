package com.disspear574.swishy.media

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.MediaStore

// Android has no common trash screen, so the vendor gallery is opened instead.
actual fun openSystemTrash() {
    val context = MediaContext.appContext ?: return
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (@Suppress("SwallowedException") ignored: ActivityNotFoundException) {
        // No gallery app, e.g. on a bare AOSP emulator.
    }
}
