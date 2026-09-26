package com.disspear574.swishy.decisions

import com.disspear574.swishy.media.MediaAsset
import com.disspear574.swishy.media.MediaKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeckTest {

    private fun asset(id: String, sizeBytes: Long = 1_000) = MediaAsset(
        id = id,
        kind = MediaKind.PHOTO,
        takenAtMillis = 0,
        sizeBytes = sizeBytes,
        durationMillis = null,
    )

    private val three = listOf(asset("a"), asset("b"), asset("c"))

    @Test
    fun `deck starts at the first frame`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals("a", deck.current?.id)
        assertEquals(3, deck.remaining)
        assertEquals(0, deck.decided)
    }

    @Test
    fun `decision advances the deck`() {
        val deck = Deck.of(three, InMemoryDecisionStore()).decide(Decision.KEPT)

        assertEquals("b", deck.current?.id)
        assertEquals(1, deck.decided)
        assertEquals(2, deck.remaining)
    }

    @Test
    fun `already decided frames are left out of the deck`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.KEPT, sizeBytes = 1_000)

        val deck = Deck.of(three, store)

        assertEquals("b", deck.current?.id)
        assertEquals(2, deck.remaining)
    }

    @Test
    fun `deck is empty after the last frame`() {
        var deck = Deck.of(three, InMemoryDecisionStore())
        repeat(3) { deck = deck.decide(Decision.KEPT) }

        assertNull(deck.current)
        assertEquals(0, deck.remaining)
    }

    @Test
    fun `undo reverts the decision and brings back the same frame`() {
        val store = InMemoryDecisionStore()
        val deck = Deck.of(three, store)
            .decide(Decision.TRASHED)
            .undo()

        assertEquals("a", deck.current?.id)
        assertNull(store.decisionOf("a"))
        assertEquals(0, store.trashedBytes())
    }

    @Test
    fun `undo on an empty history does nothing`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals("a", deck.undo().current?.id)
    }

    @Test
    fun `trashed frames are counted in bytes`() {
        val store = InMemoryDecisionStore()
        Deck.of(listOf(asset("a", sizeBytes = 4_000)), store).decide(Decision.TRASHED)

        assertEquals(4_000, store.trashedBytes())
    }

    @Test
    fun `size can be passed explicitly when it is known later`() {
        val store = InMemoryDecisionStore()
        Deck.of(listOf(asset("a", sizeBytes = 0)), store)
            .decide(Decision.TRASHED, sizeBytes = 7_000)

        assertEquals(7_000, store.trashedBytes())
    }

    @Test
    fun `upcoming frames start with the current one`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals(listOf("a", "b", "c"), deck.upcoming(3).map { it.id })
    }

    @Test
    fun `upcoming frames never exceed the remaining ones`() {
        val deck = Deck.of(three, InMemoryDecisionStore()).decide(Decision.KEPT)

        assertEquals(listOf("b", "c"), deck.upcoming(5).map { it.id })
    }

    @Test
    fun `timeline starts as all nulls`() {
        assertEquals(listOf(null, null, null), Deck.of(three, InMemoryDecisionStore()).timeline())
    }

    @Test
    fun `timeline keeps decisions in the order they were made`() {
        val deck = Deck.of(three, InMemoryDecisionStore())
            .decide(Decision.TRASHED)
            .decide(Decision.KEPT)

        assertEquals(listOf(Decision.TRASHED, Decision.KEPT, null), deck.timeline())
    }

    @Test
    fun `undo removes the decision from the timeline`() {
        val deck = Deck.of(three, InMemoryDecisionStore())
            .decide(Decision.TRASHED)
            .undo()

        assertEquals(listOf(null, null, null), deck.timeline())
    }

    @Test
    fun `total frame count does not change with decisions`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals(3, deck.total)
        assertEquals(3, deck.decide(Decision.KEPT).total)
    }

    @Test
    fun `moved frames are shown only where requested`() {
        val store = InMemoryDecisionStore().apply {
            record("b", Decision.MOVED, sizeBytes = 1)
            record("c", Decision.TRASHED, sizeBytes = 1)
        }

        val months = Deck.of(three, store)
        val album = Deck.of(three, store, showDecided = setOf(Decision.MOVED))

        assertEquals(listOf("a"), months.upcoming(3).map { it.id })
        assertEquals(listOf("a", "b"), album.upcoming(3).map { it.id })
    }
}
