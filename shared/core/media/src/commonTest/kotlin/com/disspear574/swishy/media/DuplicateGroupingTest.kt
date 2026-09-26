package com.disspear574.swishy.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DuplicateGroupingTest {

    private fun asset(id: String, d: Long, p: Long) = HashedAsset(id, ImageHash(d, p))

    private val farD = 0x5555_5555_5555_5555L
    private val farP = -0x5555_5555_5555_5556L

    private fun flip(value: Long, bits: Int): Long {
        var result = value
        repeat(bits) { index -> result = result xor (1L shl index) }
        return result
    }

    @Test
    fun `assets with equal hashes form an identical group`() {
        val groups = groupDuplicates(
            // Truly far: small numbers such as 1 and 99 differ in only three bits.
            listOf(asset("a", 1L, 2L), asset("b", 1L, 2L), asset("c", farD, farP)),
        )

        assertEquals(1, groups.size)
        assertEquals(DuplicateKind.IDENTICAL, groups.single().kind)
        assertEquals(listOf("a", "b"), groups.single().ids)
    }

    @Test
    fun `a single asset forms no group`() {
        val groups = groupDuplicates(listOf(asset("a", 1L, 2L)))

        assertTrue(groups.isEmpty(), "got: $groups")
    }

    @Test
    fun `both thresholds must pass`() {
        // Equal dHash with a far pHash means different frames.
        val groups = groupDuplicates(
            listOf(asset("a", 0L, 0L), asset("b", 0L, flip(0L, 20))),
        )

        assertTrue(groups.isEmpty(), "got: $groups")
    }

    @Test
    fun `hashes at the dHash threshold are grouped when every byte differs`() {
        // One changed bit in each byte: distance 8, and no 8-bit slice of the two hashes is equal.
        val spread = 0x0101_0101_0101_0101L

        val groups = groupDuplicates(listOf(asset("a", 0L, 0L), asset("b", spread, 0L)))

        assertEquals(1, groups.size)
        assertEquals(listOf("a", "b"), groups.single().ids)
    }

    @Test
    fun `similarity carries along a chain`() {
        val a = asset("a", 0L, 0L)
        val b = asset("b", flip(0L, 5), flip(0L, 5))
        val c = asset("c", flip(0L, 10), flip(0L, 10))

        val groups = groupDuplicates(listOf(a, b, c))

        assertEquals(1, groups.size)
        assertEquals(DuplicateKind.SIMILAR, groups.single().kind)
        assertEquals(listOf("a", "b", "c"), groups.single().ids)
    }

    @Test
    fun `a cluster of identical and similar assets is marked similar`() {
        val groups = groupDuplicates(
            listOf(asset("a", 0L, 0L), asset("b", 0L, 0L), asset("c", flip(0L, 6), flip(0L, 6))),
        )

        assertEquals(DuplicateKind.SIMILAR, groups.single().kind)
        assertEquals(3, groups.single().ids.size)
    }

    @Test
    fun `identical assets are found even in an overflowing band`() {
        // Overflowing bands are skipped, but exact matches come from the hash map.
        val flat = List(BAND_OVERFLOW + 50) { index -> asset("flat$index", 0L, 0L) }

        val groups = groupDuplicates(flat)

        assertEquals(1, groups.size)
        assertEquals(DuplicateKind.IDENTICAL, groups.single().kind)
        assertEquals(flat.size, groups.single().ids.size)
    }
}
