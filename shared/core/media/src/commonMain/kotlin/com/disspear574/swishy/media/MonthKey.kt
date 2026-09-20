package com.disspear574.swishy.media

data class MonthKey(val year: Int, val month: Int)

data class MonthSummary(
    val month: MonthKey,
    val count: Int,
    val sizeBytes: Long,
)
