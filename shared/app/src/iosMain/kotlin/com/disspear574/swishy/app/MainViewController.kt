package com.disspear574.swishy.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.window.ComposeUIViewController
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.media.IosMediaLibrary
import com.disspear574.swishy.media.MediaIndex
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import platform.UIKit.UIViewController

private val lifecycle = LifecycleRegistry()
private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
private val component = GalleryComponent(DefaultComponentContext(lifecycle = lifecycle))
private var store: DecisionStore? = null
private val library = IosMediaLibrary()
private val index = MediaIndex(library = library, scope = scope)

fun mainViewController(): UIViewController {
    lifecycle.resume()
    return ComposeUIViewController {
        val ready: DecisionStore? by produceState<DecisionStore?>(initialValue = store) {
            value = store ?: createStore(scope).also { store = it }
        }
        ready?.let { loaded ->
            App(
                component = component,
                library = library,
                index = index,
                store = loaded,
            )
        }
    }
}
