package com.disspear574.swishy.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.disspear574.swishy.app.App
import com.disspear574.swishy.app.createDecisionStore
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.media.AndroidMediaLibrary
import com.arkivanov.decompose.retainedComponent

class MainActivity : ComponentActivity() {

    private val requestHost = ActivitySystemRequestHost(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val library = AndroidMediaLibrary(context = this, requestHost = requestHost)
        val component = retainedComponent { context -> GalleryComponent(context) }
        setContent {
            App(component = component, library = library, store = DecisionStoreHolder.store)
        }
    }
}

private object DecisionStoreHolder {
    val store = createDecisionStore()
}
