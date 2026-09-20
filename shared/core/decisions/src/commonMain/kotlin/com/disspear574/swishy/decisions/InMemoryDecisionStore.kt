package com.disspear574.swishy.decisions

class InMemoryDecisionStore : DecisionStore {

    private data class Record(val decision: Decision, val sizeBytes: Long)

    private val records = LinkedHashMap<String, Record>()

    override fun decisionOf(id: String): Decision? = records[id]?.decision

    override fun record(id: String, decision: Decision, sizeBytes: Long) {
        records[id] = Record(decision = decision, sizeBytes = sizeBytes)
    }

    override fun forget(id: String) {
        records.remove(id)
    }

    override fun trashedIds(): List<String> =
        records.filterValues { it.decision == Decision.TRASHED }.keys.toList()

    override fun trashedBytes(): Long =
        records.values.filter { it.decision == Decision.TRASHED }.sumOf { it.sizeBytes }
}
