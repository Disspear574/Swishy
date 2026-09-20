package com.disspear574.swishy.app

import androidx.compose.runtime.Composable
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.InMemoryDecisionStore
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.gallery.GalleryScreen
import com.disspear574.swishy.media.MediaLibrary

@Composable
fun App(
    component: GalleryComponent,
    library: MediaLibrary,
    store: DecisionStore,
) {
    SwishyTheme {
        GalleryScreen(component = component, library = library, store = store)
    }
}

fun createDecisionStore(): DecisionStore = InMemoryDecisionStore()
