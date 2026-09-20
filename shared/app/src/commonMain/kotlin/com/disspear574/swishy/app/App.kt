package com.disspear574.swishy.app

import androidx.compose.runtime.Composable
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.db.createDatabase
import com.disspear574.swishy.decisions.db.createDecisionStore
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.gallery.GalleryScreen
import com.disspear574.swishy.media.MediaLibrary
import kotlinx.coroutines.CoroutineScope

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

suspend fun createStore(scope: CoroutineScope): DecisionStore =
    createDecisionStore(database = createDatabase(), scope = scope)
