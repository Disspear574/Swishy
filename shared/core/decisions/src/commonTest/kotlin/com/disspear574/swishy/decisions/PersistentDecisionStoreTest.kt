package com.disspear574.swishy.decisions

import com.disspear574.swishy.decisions.db.DecisionDao
import com.disspear574.swishy.decisions.db.DecisionEntity
import com.disspear574.swishy.decisions.db.PersistentDecisionStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PersistentDecisionStoreTest {

    private class FakeDao(rows: List<DecisionEntity> = emptyList()) : DecisionDao {
        val stored = rows.associateBy { it.assetId }.toMutableMap()

        override suspend fun all(): List<DecisionEntity> = stored.values.toList()
        override suspend fun put(entity: DecisionEntity) {
            stored[entity.assetId] = entity
        }

        override suspend fun remove(assetId: String) {
            stored.remove(assetId)
        }

        override suspend fun removeAll(assetIds: List<String>) {
            assetIds.forEach(stored::remove)
        }

        override suspend fun clear() {
            stored.clear()
        }
    }

    private fun entity(id: String, decision: Decision, size: Long = 1_000) = DecisionEntity(
        assetId = id,
        decision = decision.name,
        decidedAt = 0,
        sizeBytes = size,
    )

    @Test
    fun `stored decisions are visible right after creation`() = runTest {
        val dao = FakeDao(listOf(entity("a", Decision.KEPT), entity("b", Decision.TRASHED, 2_000)))

        val store = PersistentDecisionStore(dao = dao, scope = TestScope(), initial = dao.all())

        assertEquals(Decision.KEPT, store.decisionOf("a"))
        assertEquals(listOf("b"), store.trashedIds())
        assertEquals(2_000, store.trashedBytes())
    }

    @Test
    fun `new decision is written to the database`() = runTest {
        val dao = FakeDao()
        val store = PersistentDecisionStore(dao = dao, scope = this, initial = emptyList())

        store.record("a", Decision.TRASHED, sizeBytes = 3_000)
        testScheduler.advanceUntilIdle()

        assertEquals(Decision.TRASHED.name, dao.stored["a"]?.decision)
        assertEquals(3_000, dao.stored["a"]?.sizeBytes)
    }

    @Test
    fun `decision is in the database after commit without advancing the scheduler`() = runTest {
        val dao = FakeDao()
        val store = PersistentDecisionStore(dao = dao, scope = this, initial = emptyList())

        store.record("a", Decision.TRASHED, sizeBytes = 3_000)
        store.commit()

        assertEquals(Decision.TRASHED.name, dao.stored["a"]?.decision)
    }

    @Test
    fun `writes reach the database in decision order`() = runTest {
        val dao = FakeDao()
        val store = PersistentDecisionStore(dao = dao, scope = this, initial = emptyList())

        store.record("a", Decision.TRASHED, sizeBytes = 1)
        store.record("a", Decision.KEPT, sizeBytes = 1)
        store.commit()

        assertEquals(Decision.KEPT.name, dao.stored["a"]?.decision)
    }

    @Test
    fun `forgotten decision is removed from the database`() = runTest {
        val dao = FakeDao(listOf(entity("a", Decision.TRASHED)))
        val store = PersistentDecisionStore(dao = dao, scope = this, initial = dao.all())

        store.forget("a")
        testScheduler.advanceUntilIdle()

        assertNull(store.decisionOf("a"))
        assertNull(dao.stored["a"])
    }

    @Test
    fun `unknown decision value is ignored rather than treated as a decision`() = runTest {
        val dao = FakeDao(
            listOf(DecisionEntity(assetId = "a", decision = "GARBAGE", decidedAt = 0, sizeBytes = 1)),
        )

        val store = PersistentDecisionStore(dao = dao, scope = TestScope(), initial = dao.all())

        assertNull(store.decisionOf("a"))
    }
}
