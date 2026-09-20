package com.disspear574.swishy.media

import android.content.Context

actual object MediaContext {

    @Volatile
    internal var appContext: Context? = null

    actual val isReady: Boolean get() = appContext != null

    fun install(context: Context) {
        appContext = context.applicationContext
    }
}
