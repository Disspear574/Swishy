package com.disspear574.swishy.android

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.arkivanov.decompose.retainedComponent
import com.disspear574.swishy.app.App
import com.disspear574.swishy.app.SwishyStores
import com.disspear574.swishy.app.createStores
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.media.AndroidMediaLibrary
import com.disspear574.swishy.media.MediaIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {

    // Initialized with the activity: registering result launchers after onStart crashes.
    private val requestHost = ActivitySystemRequestHost(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Without it the system bars get their own fill and cut off the app background.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        enableHighDynamicRange()

        val library = AndroidMediaLibrary(context = this, requestHost = requestHost)
        val index = MediaHolder.index(library)
        // Retained across rotation so the navigation stack is not rebuilt.
        val component = retainedComponent { context -> GalleryComponent(context) }

        setContent {
            // Nothing is shown until decisions load: an empty history would offer sorted photos again.
            val stores: SwishyStores? by produceState<SwishyStores?>(initialValue = null) {
                value = StoreHolder.get()
            }
            stores?.let { ready ->
                App(
                    component = component,
                    library = library,
                    index = index,
                    stores = ready,
                )
            }
        }
    }
}

private fun ComponentActivity.enableHighDynamicRange() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
    // Ultra HDR is tone-mapped to SDR unless the window is in HDR mode; SDR displays would only waste power.
    val supported = display?.isHdr == true
    if (supported) {
        window.colorMode = ActivityInfo.COLOR_MODE_HDR
    }
}

/** Keeps the library snapshot across activity recreation so rotation does not rescan it. */
private object MediaHolder {

    private var cached: MediaIndex? = null

    fun index(library: AndroidMediaLibrary): MediaIndex =
        cached ?: MediaIndex(library = library, scope = StoreHolder.scope).also { cached = it }
}

/** Keeps the database stores across activity recreation. */
private object StoreHolder {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var stores: SwishyStores? = null

    suspend fun get(): SwishyStores = stores ?: createStores(scope).also { stores = it }
}
