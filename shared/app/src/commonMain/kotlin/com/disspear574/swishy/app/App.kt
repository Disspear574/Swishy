package com.disspear574.swishy.app

import androidx.compose.runtime.Composable
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.db.HashStore
import com.disspear574.swishy.decisions.db.createDatabase
import com.disspear574.swishy.decisions.db.createDecisionStore
import com.disspear574.swishy.decisions.db.createHashStore
import com.disspear574.swishy.designsystem.theme.SwishyTheme
import com.disspear574.swishy.gallery.GalleryComponent
import com.disspear574.swishy.gallery.GalleryScreen
import com.disspear574.swishy.media.MediaIndex
import com.disspear574.swishy.media.MediaLibrary
import kotlinx.coroutines.CoroutineScope

@Composable
fun App(
    component: GalleryComponent,
    library: MediaLibrary,
    index: MediaIndex,
    stores: SwishyStores,
) {
    SwishyTheme {
        GalleryScreen(
            component = component,
            library = library,
            index = index,
            store = stores.decisions,
            hashStore = stores.hashes,
        )
    }
}

/** Database-backed stores; one per process so the file never has two connections. */
class SwishyStores(val decisions: DecisionStore, val hashes: HashStore)

suspend fun createStores(scope: CoroutineScope): SwishyStores {
    val database = createDatabase()
    return SwishyStores(
        decisions = createDecisionStore(database = database, scope = scope),
        hashes = createHashStore(database),
    )
}
