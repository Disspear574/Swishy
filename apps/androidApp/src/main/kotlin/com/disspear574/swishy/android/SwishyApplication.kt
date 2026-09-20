package com.disspear574.swishy.android

import android.app.Application
import com.disspear574.swishy.decisions.db.DecisionsContext
import com.disspear574.swishy.media.MediaContext

class SwishyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MediaContext.install(this)
        DecisionsContext.install(this)
    }
}
