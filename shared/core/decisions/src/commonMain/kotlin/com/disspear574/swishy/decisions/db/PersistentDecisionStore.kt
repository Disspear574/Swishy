package com.disspear574.swishy.decisions.db

import com.disspear574.swishy.decisions.Decision
import com.disspear574.swishy.decisions.DecisionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PersistentDecisionStore internal constructor(
    private val dao: DecisionDao,
    private val scope: CoroutineScope,
    initial: List<DecisionEntity>,
) : DecisionStore {

    private data class Record(val decision: Decision, val sizeBytes: Long)

    private val records = LinkedHashMap<String, Record>().apply {
        initial.forEach { entity ->
            val decision = entity.decision.toDecision() ?: return@forEach
            put(entity.assetId, Record(decision = decision, sizeBytes = entity.sizeBytes))
        }
    }

    override fun decisionOf(id: String): Decision? = records[id]?.decision

    private var lastWrite: Job? = null

    override fun record(id: String, decision: Decision, sizeBytes: Long) {
        records[id] = Record(decision = decision, sizeBytes = sizeBytes)
        enqueue {
            dao.put(
                DecisionEntity(
                    assetId = id,
                    decision = decision.name,
                    decidedAt = nowMillis(),
                    sizeBytes = sizeBytes,
                ),
            )
        }
    }

    override fun forget(id: String) {
        records.remove(id)
        enqueue { dao.remove(id) }
    }

    override suspend fun commit() {
        lastWrite?.join()
    }

    private fun enqueue(write: suspend () -> Unit) {
        val previous = lastWrite
        lastWrite = scope.launch {
            previous?.join()
            write()
        }
    }

    override fun trashedIds(): List<String> =
        records.filterValues { it.decision == Decision.TRASHED }.keys.toList()

    override fun trashedBytes(): Long =
        records.values.filter { it.decision == Decision.TRASHED }.sumOf { it.sizeBytes }

    private fun String.toDecision(): Decision? = when (this) {
        Decision.KEPT.name -> Decision.KEPT
        Decision.TRASHED.name -> Decision.TRASHED
        else -> null
    }
}

internal expect fun nowMillis(): Long
