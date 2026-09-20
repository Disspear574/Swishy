package com.disspear574.swishy.media

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthGroupingTest {

    private fun photo(id: String, takenAtMillis: Long, sizeBytes: Long = 1_000) = MediaAsset(
        id = id,
        kind = MediaKind.PHOTO,
        takenAtMillis = takenAtMillis,
        sizeBytes = sizeBytes,
        durationMillis = null,
    )

    /** 2026-09-20T12:00:00Z */
    private val september = 1_789_905_600_000L

    private val augustLateUtc = 1_788_217_200_000L

    @Test
    fun `собирает ассеты одного месяца в одну сводку`() {
        val months = listOf(
            photo("a", september, sizeBytes = 2_000),
            photo("b", september + 1_000, sizeBytes = 3_000),
        ).groupIntoMonths(TimeZone.UTC)

        assertEquals(1, months.size)
        assertEquals(MonthKey(year = 2026, month = 9), months.single().month)
        assertEquals(2, months.single().count)
        assertEquals(5_000, months.single().sizeBytes)
    }

    @Test
    fun `свежие месяцы идут первыми`() {
        val months = listOf(photo("old", augustLateUtc), photo("new", september))
            .groupIntoMonths(TimeZone.UTC)

        assertEquals(listOf(MonthKey(2026, 9), MonthKey(2026, 8)), months.map { it.month })
    }

    @Test
    fun `месяц определяется местной датой, а не UTC`() {
        val moscow = TimeZone.of("Europe/Moscow")

        val months = listOf(photo("edge", augustLateUtc)).groupIntoMonths(moscow)

        assertEquals(MonthKey(year = 2026, month = 9), months.single().month)
    }

    @Test
    fun `пустой список даёт пустой результат`() {
        assertEquals(emptyList(), emptyList<MediaAsset>().groupIntoMonths(TimeZone.UTC))
    }

    @Test
    fun `ключ месяца одного ассета совпадает с ключом его сводки`() {
        val asset = photo("a", september)

        assertEquals(
            asset.monthKeyIn(TimeZone.UTC),
            listOf(asset).groupIntoMonths(TimeZone.UTC).single().month,
        )
    }
}
