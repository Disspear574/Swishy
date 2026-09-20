package com.disspear574.swishy.decisions

interface DecisionStore {

    fun decisionOf(id: String): Decision?

    fun record(id: String, decision: Decision, sizeBytes: Long)

    fun forget(id: String)

    fun trashedIds(): List<String>

    fun trashedBytes(): Long
}
