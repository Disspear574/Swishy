package com.disspear574.swishy.media

/** A month in the local calendar; [month] is 1..12. */
data class MonthKey(val year: Int, val month: Int)

data class MonthSummary(
    val month: MonthKey,
    val count: Int,
    val sizeBytes: Long,
)
