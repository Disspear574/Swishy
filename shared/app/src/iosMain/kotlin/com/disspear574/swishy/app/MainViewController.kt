package com.disspear574.swishy.app

import androidx.compose.ui.window.ComposeUIViewController
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.media.IosMediaLibrary
import platform.UIKit.UIViewController

private val store: DecisionStore = createDecisionStore()

fun mainViewController(): UIViewController = ComposeUIViewController {
    App(library = IosMediaLibrary(), store = store)
}
