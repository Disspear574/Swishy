package com.disspear574.swishy.app

import androidx.compose.ui.window.ComposeUIViewController
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.media.IosMediaLibrary
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import platform.UIKit.UIViewController

private val lifecycle = LifecycleRegistry()
private val store: DecisionStore = createDecisionStore()
private val component = GalleryComponent(DefaultComponentContext(lifecycle = lifecycle))

fun mainViewController(): UIViewController {
    lifecycle.resume()
    return ComposeUIViewController {
        App(component = component, library = IosMediaLibrary(), store = store)
    }
}
