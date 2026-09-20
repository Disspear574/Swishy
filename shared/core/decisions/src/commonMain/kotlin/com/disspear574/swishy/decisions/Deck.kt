package com.disspear574.swishy.decisions

import com.disspear574.swishy.media.MediaAsset

class Deck private constructor(
    private val pending: List<MediaAsset>,
    private val store: DecisionStore,
    private val history: List<MediaAsset>,
) {

    val current: MediaAsset? get() = pending.firstOrNull()

    val remaining: Int get() = pending.size

    val decided: Int get() = history.size

    val total: Int get() = pending.size + history.size

    fun decide(decision: Decision): Deck {
        val asset = current ?: return this
        store.record(id = asset.id, decision = decision, sizeBytes = asset.sizeBytes)
        return Deck(pending = pending.drop(1), store = store, history = history + asset)
    }

    fun undo(): Deck {
        val asset = history.lastOrNull() ?: return this
        store.forget(asset.id)
        return Deck(
            pending = listOf(asset) + pending,
            store = store,
            history = history.dropLast(1),
        )
    }

    companion object {

        fun of(assets: List<MediaAsset>, store: DecisionStore): Deck = Deck(
            pending = assets.filter { store.decisionOf(it.id) == null },
            store = store,
            history = emptyList(),
        )
    }
}
