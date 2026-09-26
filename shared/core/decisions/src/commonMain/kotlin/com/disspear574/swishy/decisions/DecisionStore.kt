package com.disspear574.swishy.decisions

/** Permanent per-frame decisions with sizes, so the trash total needs no media library query. */
interface DecisionStore {

    fun decisionOf(id: String): Decision?

    fun record(id: String, decision: Decision, sizeBytes: Long)

    // Awaited before the next frame so a decision survives a crash right after the swipe.
    suspend fun commit() = Unit

    fun forget(id: String)

    fun forgetAll(decision: Decision)

    fun count(decision: Decision): Int

    fun trashedIds(): List<String>

    fun trashedBytes(): Long
}
