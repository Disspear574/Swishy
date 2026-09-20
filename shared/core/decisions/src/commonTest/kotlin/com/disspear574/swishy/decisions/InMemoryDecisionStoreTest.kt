package com.disspear574.swishy.decisions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryDecisionStoreTest {

    @Test
    fun `незнакомый ассет не имеет решения`() {
        assertNull(InMemoryDecisionStore().decisionOf("a"))
    }

    @Test
    fun `решение запоминается`() {
        val store = InMemoryDecisionStore()
        store.record(id = "a", decision = Decision.TRASHED, sizeBytes = 2_000)

        assertEquals(Decision.TRASHED, store.decisionOf("a"))
    }

    @Test
    fun `объём корзины складывается только из помеченного`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)
        store.record("b", Decision.KEPT, sizeBytes = 9_000)
        store.record("c", Decision.TRASHED, sizeBytes = 3_000)

        assertEquals(5_000, store.trashedBytes())
        assertEquals(listOf("a", "c"), store.trashedIds())
    }

    @Test
    fun `повторное решение по тому же ассету заменяет прежнее, не удваивая объём`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)

        assertEquals(2_000, store.trashedBytes())
    }

    @Test
    fun `забытое решение исчезает вместе со своим объёмом`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)
        store.forget("a")

        assertNull(store.decisionOf("a"))
        assertEquals(0, store.trashedBytes())
    }
}
