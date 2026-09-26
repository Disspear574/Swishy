package com.disspear574.swishy.gallery

import androidx.compose.runtime.Composable
import com.disspear574.swishy.decisions.SizeText
import com.disspear574.swishy.decisions.SizeUnit
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.decimal_separator
import com.disspear574.swishy.strings.unit_bytes
import com.disspear574.swishy.strings.unit_gigabytes
import com.disspear574.swishy.strings.unit_kilobytes
import com.disspear574.swishy.strings.unit_megabytes
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SizeText.label(): String {
    val unitText = stringResource(
        when (unit) {
            SizeUnit.BYTES -> Res.string.unit_bytes
            SizeUnit.KILOBYTES -> Res.string.unit_kilobytes
            SizeUnit.MEGABYTES -> Res.string.unit_megabytes
            SizeUnit.GIGABYTES -> Res.string.unit_gigabytes
        },
    )
    val number = if (tenths == 0) "$whole" else "$whole${stringResource(Res.string.decimal_separator)}$tenths"
    return "$number $unitText"
}
