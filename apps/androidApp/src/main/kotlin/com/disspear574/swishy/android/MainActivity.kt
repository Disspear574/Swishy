package com.disspear574.swishy.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.disspear574.swishy.app.App
import com.disspear574.swishy.app.createDecisionStore
import com.disspear574.swishy.media.AndroidMediaLibrary

class MainActivity : ComponentActivity() {

    private val requestHost = ActivitySystemRequestHost(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val library = AndroidMediaLibrary(context = this, requestHost = requestHost)
        setContent {
            App(library = library, store = DecisionStoreHolder.store)
        }
    }
}

private object DecisionStoreHolder {
    val store = createDecisionStore()
}
