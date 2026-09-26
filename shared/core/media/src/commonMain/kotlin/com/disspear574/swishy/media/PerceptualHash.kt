package com.disspear574.swishy.media

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

/** Two 64-bit perceptual hashes; requiring both to match keeps dark flat frames from clustering. */
data class ImageHash(val dHash: Long, val pHash: Long)

fun perceptualHash(gray: IntArray, side: Int): ImageHash {
    require(gray.size == side * side) { "expected a ${side}x$side thumbnail" }
    return ImageHash(dHash = dHash(gray, side), pHash = pHash(gray, side))
}

fun hammingDistance(first: Long, second: Long): Int = (first xor second).countOneBits()

private fun dHash(gray: IntArray, side: Int): Long {
    val small = resample(gray, side, DHASH_WIDTH, DHASH_HEIGHT)
    var hash = 0L
    var bit = 0
    for (y in 0 until DHASH_HEIGHT) {
        for (x in 0 until DHASH_WIDTH - 1) {
            val left = small[y * DHASH_WIDTH + x]
            val right = small[y * DHASH_WIDTH + x + 1]
            if (left > right) hash = hash or (1L shl bit)
            bit++
        }
    }
    return hash
}

private fun pHash(gray: IntArray, side: Int): Long {
    val coefficients = DoubleArray(PHASH_SIDE * PHASH_SIDE)
    for (v in 0 until PHASH_SIDE) {
        for (u in 0 until PHASH_SIDE) {
            coefficients[v * PHASH_SIDE + u] = dctCoefficient(gray, side, u, v)
        }
    }

    // Exact zeros come out as +/-1e-11 with a random sign and would flip bits on flat frames.
    val strongest = coefficients.drop(1).maxOf { abs(it) }
    val noiseFloor = strongest * NOISE_FRACTION
    val cleaned = DoubleArray(coefficients.size) { index ->
        if (abs(coefficients[index]) < noiseFloor) 0.0 else coefficients[index]
    }

    val withoutDc = cleaned.drop(1)
    val median = withoutDc.sorted().let { sorted ->
        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
    }

    var hash = 0L
    cleaned.forEachIndexed { index, value ->
        // Bit 0 is the DC term (mean brightness) and stays zero, so brightening keeps the hash.
        if (index != 0 && value > median) hash = hash or (1L shl index)
    }
    return hash
}

private fun dctCoefficient(gray: IntArray, side: Int, u: Int, v: Int): Double {
    var sum = 0.0
    for (y in 0 until side) {
        val cosY = cos((2 * y + 1) * v * PI / (2 * side))
        for (x in 0 until side) {
            sum += gray[y * side + x] * cos((2 * x + 1) * u * PI / (2 * side)) * cosY
        }
    }
    return sum * scale(u) * scale(v)
}

private fun scale(index: Int): Double = if (index == 0) 1.0 / sqrt(2.0) else 1.0

private fun resample(gray: IntArray, side: Int, width: Int, height: Int): IntArray =
    IntArray(width * height) { index ->
        val x = index % width
        val y = index / width
        blockAverage(
            gray = gray,
            side = side,
            fromX = x * side / width,
            toX = ((x + 1) * side / width).coerceAtLeast(x * side / width + 1),
            fromY = y * side / height,
            toY = ((y + 1) * side / height).coerceAtLeast(y * side / height + 1),
        )
    }

@Suppress("LongParameterList")
private fun blockAverage(
    gray: IntArray,
    side: Int,
    fromX: Int,
    toX: Int,
    fromY: Int,
    toY: Int,
): Int {
    var sum = 0
    var count = 0
    for (y in fromY until toY) {
        for (x in fromX until toX) {
            sum += gray[y * side + x]
            count++
        }
    }
    return sum / count
}

private const val DHASH_WIDTH = 9
private const val DHASH_HEIGHT = 8
private const val PHASH_SIDE = 8

private const val NOISE_FRACTION = 1e-6
