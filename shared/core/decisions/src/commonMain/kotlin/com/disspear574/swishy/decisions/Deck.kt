package com.disspear574.swishy.decisions

import com.disspear574.swishy.media.MediaAsset

/** Immutable deck of undecided frames with this session's decisions kept for undo. */
class Deck private constructor(
    private val pending: List<MediaAsset>,
    private val store: DecisionStore,
    private val history: List<MediaAsset>,
) {

    val current: MediaAsset? get() = pending.firstOrNull()

    val remaining: Int get() = pending.size

    val decided: Int get() = history.size

    val lastDecided: MediaAsset? get() = history.lastOrNull()

    val total: Int get() = pending.size + history.size

    fun upcoming(count: Int): List<MediaAsset> = pending.take(count)

    fun timeline(): List<Decision?> =
        history.map { asset -> store.decisionOf(asset.id) } + List(pending.size) { null }

    // On iOS the real size arrives after the scan, so callers may pass it explicitly.
    fun decide(decision: Decision, sizeBytes: Long = current?.sizeBytes ?: 0): Deck {
        val asset = current ?: return this
        store.record(id = asset.id, decision = decision, sizeBytes = sizeBytes)
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

        fun of(
            assets: List<MediaAsset>,
            store: DecisionStore,
            showDecided: Set<Decision> = emptySet(),
        ): Deck = Deck(
            pending = assets.filter { asset ->
                val decision = store.decisionOf(asset.id)
                decision == null || decision in showDecided
            },
            store = store,
            history = emptyList(),
        )
    }
}
