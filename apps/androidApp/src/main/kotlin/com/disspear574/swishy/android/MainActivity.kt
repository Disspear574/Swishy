package com.disspear574.swishy.android

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.disspear574.swishy.app.App
import com.disspear574.swishy.app.createStore
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.media.AndroidMediaLibrary
import com.arkivanov.decompose.retainedComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {

    private val requestHost = ActivitySystemRequestHost(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        enableHighDynamicRange()

        val library = AndroidMediaLibrary(context = this, requestHost = requestHost)
        val component = retainedComponent { context -> GalleryComponent(context) }

        setContent {
            val store: DecisionStore? by produceState<DecisionStore?>(initialValue = null) {
                value = StoreHolder.get()
            }
            store?.let { ready ->
                App(component = component, library = library, store = ready)
            }
        }
    }
}

private fun ComponentActivity.enableHighDynamicRange() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
    val supported = display?.isHdr == true
    if (supported) {
        window.colorMode = ActivityInfo.COLOR_MODE_HDR
    }
}

private object StoreHolder {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var store: DecisionStore? = null

    suspend fun get(): DecisionStore = store ?: createStore(scope).also { store = it }
}
