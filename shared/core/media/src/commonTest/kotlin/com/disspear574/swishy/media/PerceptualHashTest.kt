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
    fun `одинаковые кадры дают одинаковые хеши`() {
        val a = image { x, y -> x * 3 + y * 5 }
        val b = image { x, y -> x * 3 + y * 5 }

        val first = perceptualHash(a, side)
        val second = perceptualHash(b, side)

        assertEquals(0, hammingDistance(first.dHash, second.dHash))
        assertEquals(0, hammingDistance(first.pHash, second.pHash))
    }

    @Test
    fun `осветление кадра не меняет ни один из хешей`() {
        val original = image { x, y -> 40 + x * 2 + y }
        val brighter = image { x, y -> 40 + x * 2 + y + 30 }

        val first = perceptualHash(original, side)
        val second = perceptualHash(brighter, side)

        assertEquals(0, hammingDistance(first.dHash, second.dHash))
        assertEquals(0, hammingDistance(first.pHash, second.pHash))
    }

    @Test
    fun `разные кадры расходятся далеко`() {
        val ramp = image { x, _ -> x * 8 }
        val rings = image { x, y ->
            (128 + 120 * sin((x * x + y * y) / 40.0)).toInt()
        }

        val first = perceptualHash(ramp, side)
        val second = perceptualHash(rings, side)

        assertTrue(
            hammingDistance(first.dHash, second.dHash) > 8,
            "dHash слишком близки: ${hammingDistance(first.dHash, second.dHash)}",
        )
    }

    @Test
    fun `второй хеш различает кадры, которые первый считает одинаковыми`() {
        val smooth = image { x, _ -> 10 + x * 4 }
        val waved = image { x, y -> (10 + x * 4 + 50 * sin(y / 5.0)).toInt() }

        val first = perceptualHash(smooth, side)
        val second = perceptualHash(waved, side)

        assertEquals(
            0,
            hammingDistance(first.dHash, second.dHash),
            "подготовка теста неверна: dHash обязан совпасть",
        )
        assertTrue(
            hammingDistance(first.pHash, second.pHash) > 8,
            "pHash не различил кадры: ${hammingDistance(first.pHash, second.pHash)}",
        )
    }

    @Test
    fun `расстояние Хэмминга считает различающиеся биты`() {
        assertEquals(0, hammingDistance(0b1011L, 0b1011L))
        assertEquals(2, hammingDistance(0b1011L, 0b0001L))
    }
}
