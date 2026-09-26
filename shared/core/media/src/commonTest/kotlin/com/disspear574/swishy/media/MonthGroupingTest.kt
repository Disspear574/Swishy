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

    private val september = 1_789_905_600_000L

    // 31 Aug 2026 23:00 UTC, already September in Moscow.
    private val augustLateUtc = 1_788_217_200_000L

    @Test
    fun `assets of one month form one summary`() {
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
    fun `recent months come first`() {
        val months = listOf(photo("old", augustLateUtc), photo("new", september))
            .groupIntoMonths(TimeZone.UTC)

        assertEquals(listOf(MonthKey(2026, 9), MonthKey(2026, 8)), months.map { it.month })
    }

    @Test
    fun `month is taken from the local date, not UTC`() {
        val moscow = TimeZone.of("Europe/Moscow")

        val months = listOf(photo("edge", augustLateUtc)).groupIntoMonths(moscow)

        assertEquals(MonthKey(year = 2026, month = 9), months.single().month)
    }

    @Test
    fun `empty list gives empty result`() {
        assertEquals(emptyList(), emptyList<MediaAsset>().groupIntoMonths(TimeZone.UTC))
    }

    @Test
    fun `month key of an asset matches its summary`() {
        val asset = photo("a", september)

        assertEquals(
            asset.monthKeyIn(TimeZone.UTC),
            listOf(asset).groupIntoMonths(TimeZone.UTC).single().month,
        )
    }
}
