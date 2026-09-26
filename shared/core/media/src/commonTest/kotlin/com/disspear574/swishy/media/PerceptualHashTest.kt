package com.disspear574.swishy.media

import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PerceptualHashTest {

    private val side = 32

    private fun image(pixel: (x: Int, y: Int) -> Int): IntArray =
        IntArray(side * side) { index -> pixel(index % side, index / side).coerceIn(0, 255) }

    @Test
    fun `equal images give equal hashes`() {
        val a = image { x, y -> x * 3 + y * 5 }
        val b = image { x, y -> x * 3 + y * 5 }

        val first = perceptualHash(a, side)
        val second = perceptualHash(b, side)

        assertEquals(0, hammingDistance(first.dHash, second.dHash))
        assertEquals(0, hammingDistance(first.pHash, second.pHash))
    }

    @Test
    fun `brightening changes neither hash`() {
        val original = image { x, y -> 40 + x * 2 + y }
        val brighter = image { x, y -> 40 + x * 2 + y + 30 }

        val first = perceptualHash(original, side)
        val second = perceptualHash(brighter, side)

        assertEquals(0, hammingDistance(first.dHash, second.dHash))
        assertEquals(0, hammingDistance(first.pHash, second.pHash))
    }

    @Test
    fun `different images are far apart`() {
        val ramp = image { x, _ -> x * 8 }
        val rings = image { x, y ->
            (128 + 120 * sin((x * x + y * y) / 40.0)).toInt()
        }

        val first = perceptualHash(ramp, side)
        val second = perceptualHash(rings, side)

        assertTrue(
            hammingDistance(first.dHash, second.dHash) > 8,
            "dHash too close: ${hammingDistance(first.dHash, second.dHash)}",
        )
    }

    @Test
    fun `the second hash separates images the first considers equal`() {
        // Same horizontal ramp gives equal dHash; a low-frequency vertical wave moves pHash.
        val smooth = image { x, _ -> 10 + x * 4 }
        val waved = image { x, y -> (10 + x * 4 + 50 * sin(y / 5.0)).toInt() }

        val first = perceptualHash(smooth, side)
        val second = perceptualHash(waved, side)

        assertEquals(
            0,
            hammingDistance(first.dHash, second.dHash),
            "test setup is wrong: dHash must match",
        )
        assertTrue(
            hammingDistance(first.pHash, second.pHash) > 8,
            "pHash did not separate the images: ${hammingDistance(first.pHash, second.pHash)}",
        )
    }

    @Test
    fun `Hamming distance counts differing bits`() {
        assertEquals(0, hammingDistance(0b1011L, 0b1011L))
        assertEquals(2, hammingDistance(0b1011L, 0b0001L))
    }
}
