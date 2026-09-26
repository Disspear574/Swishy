package com.disspear574.swishy.decisions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryDecisionStoreTest {

    @Test
    fun `unknown asset has no decision`() {
        assertNull(InMemoryDecisionStore().decisionOf("a"))
    }

    @Test
    fun `decision is remembered`() {
        val store = InMemoryDecisionStore()
        store.record(id = "a", decision = Decision.TRASHED, sizeBytes = 2_000)

        assertEquals(Decision.TRASHED, store.decisionOf("a"))
    }

    @Test
    fun `trash size sums only trashed frames`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)
        store.record("b", Decision.KEPT, sizeBytes = 9_000)
        store.record("c", Decision.TRASHED, sizeBytes = 3_000)

        assertEquals(5_000, store.trashedBytes())
        assertEquals(listOf("a", "c"), store.trashedIds())
    }

    @Test
    fun `repeated decision on one asset replaces the previous one without doubling the size`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)

        assertEquals(2_000, store.trashedBytes())
    }

    @Test
    fun `forgotten decision disappears with its size`() {
        val store = InMemoryDecisionStore()
        store.record("a", Decision.TRASHED, sizeBytes = 2_000)
        store.forget("a")

        assertNull(store.decisionOf("a"))
        assertEquals(0, store.trashedBytes())
    }
}
