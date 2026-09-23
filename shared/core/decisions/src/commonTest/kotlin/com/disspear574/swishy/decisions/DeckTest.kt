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
    fun `колода начинается с первого кадра`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals("a", deck.current?.id)
        assertEquals(3, deck.remaining)
        assertEquals(0, deck.decided)
    }

    @Test
    fun `решение продвигает колоду`() {
        val deck = Deck.of(three, InMemoryDecisionStore()).decide(Decision.KEPT)

        assertEquals("b", deck.current?.id)
        assertEquals(1, deck.decided)
        assertEquals(2, deck.remaining)
    }

    @Test
    fun `уже решённые кадры в колоду не попадают`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.KEPT, sizeBytes = 1_000)

        val deck = Deck.of(three, store)

        assertEquals("b", deck.current?.id)
        assertEquals(2, deck.remaining)
    }

    @Test
    fun `после последнего кадра колода пуста`() {
        var deck = Deck.of(three, InMemoryDecisionStore())
        repeat(3) { deck = deck.decide(Decision.KEPT) }

        assertNull(deck.current)
        assertEquals(0, deck.remaining)
    }

    @Test
    fun `вернуть последнее отменяет решение и возвращает тот же кадр`() {
        val store = InMemoryDecisionStore()
        val deck = Deck.of(three, store)
            .decide(Decision.TRASHED)
            .undo()

        assertEquals("a", deck.current?.id)
        assertNull(store.decisionOf("a"))
        assertEquals(0, store.trashedBytes())
    }

    @Test
    fun `вернуть последнее на пустой истории ничего не делает`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals("a", deck.undo().current?.id)
    }

    @Test
    fun `помеченное в корзину считается в байтах`() {
        val store = InMemoryDecisionStore()
        Deck.of(listOf(asset("a", sizeBytes = 4_000)), store).decide(Decision.TRASHED)

        assertEquals(4_000, store.trashedBytes())
    }

    @Test
    fun `размер можно передать явно, если он стал известен позже`() {
        val store = InMemoryDecisionStore()
        Deck.of(listOf(asset("a", sizeBytes = 0)), store)
            .decide(Decision.TRASHED, sizeBytes = 7_000)

        assertEquals(7_000, store.trashedBytes())
    }

    @Test
    fun `ближайшие кадры начинаются с текущего`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals(listOf("a", "b", "c"), deck.upcoming(3).map { it.id })
    }

    @Test
    fun `ближайших не больше, чем осталось`() {
        val deck = Deck.of(three, InMemoryDecisionStore()).decide(Decision.KEPT)

        assertEquals(listOf("b", "c"), deck.upcoming(5).map { it.id })
    }

    @Test
    fun `история решений пуста в начале и вся из null`() {
        assertEquals(listOf(null, null, null), Deck.of(three, InMemoryDecisionStore()).timeline())
    }

    @Test
    fun `история хранит решения в порядке принятия`() {
        val deck = Deck.of(three, InMemoryDecisionStore())
            .decide(Decision.TRASHED)
            .decide(Decision.KEPT)

        assertEquals(listOf(Decision.TRASHED, Decision.KEPT, null), deck.timeline())
    }

    @Test
    fun `отмена убирает решение из истории`() {
        val deck = Deck.of(three, InMemoryDecisionStore())
            .decide(Decision.TRASHED)
            .undo()

        assertEquals(listOf(null, null, null), deck.timeline())
    }

    @Test
    fun `общее число кадров не меняется от решений`() {
        val deck = Deck.of(three, InMemoryDecisionStore())

        assertEquals(3, deck.total)
        assertEquals(3, deck.decide(Decision.KEPT).total)
    }

    @Test
    fun `отложенные в альбом кадры видны только там, где об этом попросили`() {
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
