package com.disspear574.swishy.app

import androidx.compose.runtime.Composable
import com.disspear574.swishy.decisions.DecisionStore
import com.disspear574.swishy.decisions.InMemoryDecisionStore
import com.disspear574.swishy.media.MediaLibrary
import com.disspear574.swishy.probe.ProbeScreen

@Composable
fun App(library: MediaLibrary, store: DecisionStore) {
    ProbeScreen(library = library, store = store)
}

fun createDecisionStore(): DecisionStore = InMemoryDecisionStore()
