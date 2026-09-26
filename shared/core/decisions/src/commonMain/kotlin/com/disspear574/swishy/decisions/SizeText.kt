package com.disspear574.swishy.decisions

import kotlin.math.round

enum class SizeUnit { BYTES, KILOBYTES, MEGABYTES, GIGABYTES }

/** File size as a number and a unit; the UI joins them with a localized unit. */
data class SizeText(val value: String, val unit: SizeUnit)

private const val STEP = 1024.0

private const val TENTHS = 10

fun formatSize(bytes: Long): SizeText {
    if (bytes <= 0) return SizeText(value = "0", unit = SizeUnit.BYTES)

    val gigabytes = bytes / (STEP * STEP * STEP)
    val megabytes = bytes / (STEP * STEP)
    val kilobytes = bytes / STEP

    return when {
        gigabytes >= 1 -> SizeText(oneDecimal(gigabytes), SizeUnit.GIGABYTES)
        megabytes >= 1 -> SizeText(oneDecimal(megabytes), SizeUnit.MEGABYTES)
        kilobytes >= 1 -> SizeText(oneDecimal(kilobytes), SizeUnit.KILOBYTES)
        else -> SizeText(value = bytes.toString(), unit = SizeUnit.BYTES)
    }
}

// The decimal separator is not localized yet; round values drop the fraction.
private fun oneDecimal(value: Double): String {
    val tenths = round(value * TENTHS).toLong()
    val whole = tenths / TENTHS
    val fraction = tenths % TENTHS
    return if (fraction == 0L) whole.toString() else "$whole,$fraction"
}
