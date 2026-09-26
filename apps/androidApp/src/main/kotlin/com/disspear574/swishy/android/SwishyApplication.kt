package com.disspear574.swishy.android

import android.app.Application
import com.disspear574.swishy.decisions.db.DecisionsContext
import com.disspear574.swishy.media.MediaContext

class SwishyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Installed here because expect functions cannot take a Context without leaking Android into commonMain.
        MediaContext.install(this)
        DecisionsContext.install(this)
    }
}
