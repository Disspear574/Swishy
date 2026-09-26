package com.disspear574.swishy.decisions

import kotlin.math.round

enum class SizeUnit { BYTES, KILOBYTES, MEGABYTES, GIGABYTES }

/** File size as whole units and tenths; the UI adds the localized separator and unit. */
data class SizeText(val whole: Long, val tenths: Int, val unit: SizeUnit)

private const val STEP = 1024.0

private const val TENTHS = 10

fun formatSize(bytes: Long): SizeText {
    if (bytes <= 0) return SizeText(whole = 0, tenths = 0, unit = SizeUnit.BYTES)

    val gigabytes = bytes / (STEP * STEP * STEP)
    val megabytes = bytes / (STEP * STEP)
    val kilobytes = bytes / STEP

    return when {
        gigabytes >= 1 -> oneDecimal(gigabytes, SizeUnit.GIGABYTES)
        megabytes >= 1 -> oneDecimal(megabytes, SizeUnit.MEGABYTES)
        kilobytes >= 1 -> oneDecimal(kilobytes, SizeUnit.KILOBYTES)
        else -> SizeText(whole = bytes, tenths = 0, unit = SizeUnit.BYTES)
    }
}

private fun oneDecimal(value: Double, unit: SizeUnit): SizeText {
    val tenths = round(value * TENTHS).toLong()
    return SizeText(whole = tenths / TENTHS, tenths = (tenths % TENTHS).toInt(), unit = unit)
}
