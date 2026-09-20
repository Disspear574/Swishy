package com.disspear574.swishy.media

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun MediaAsset.monthKeyIn(timeZone: TimeZone): MonthKey {
    val local = Instant.fromEpochMilliseconds(takenAtMillis).toLocalDateTime(timeZone)
    return MonthKey(year = local.year, month = local.month.number)
}

fun List<MediaAsset>.groupIntoMonths(timeZone: TimeZone): List<MonthSummary> =
    groupBy { asset -> asset.monthKeyIn(timeZone) }
        .map { (month, assets) ->
            MonthSummary(
                month = month,
                count = assets.size,
                sizeBytes = assets.sumOf { it.sizeBytes },
            )
        }
        .sortedWith(
            compareByDescending<MonthSummary> { it.month.year }
                .thenByDescending { it.month.month },
        )
