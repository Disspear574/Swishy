package com.disspear574.swishy.gallery

import androidx.compose.runtime.Composable
import com.disspear574.swishy.media.MonthKey
import com.disspear574.swishy.strings.Res
import com.disspear574.swishy.strings.month_1
import com.disspear574.swishy.strings.month_10
import com.disspear574.swishy.strings.month_11
import com.disspear574.swishy.strings.month_12
import com.disspear574.swishy.strings.month_2
import com.disspear574.swishy.strings.month_3
import com.disspear574.swishy.strings.month_4
import com.disspear574.swishy.strings.month_5
import com.disspear574.swishy.strings.month_6
import com.disspear574.swishy.strings.month_7
import com.disspear574.swishy.strings.month_8
import com.disspear574.swishy.strings.month_9
import com.disspear574.swishy.strings.month_with_year
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MonthKey.displayName(): String {
    val name = stringResource(
        when (month) {
            1 -> Res.string.month_1
            2 -> Res.string.month_2
            3 -> Res.string.month_3
            4 -> Res.string.month_4
            5 -> Res.string.month_5
            6 -> Res.string.month_6
            7 -> Res.string.month_7
            8 -> Res.string.month_8
            9 -> Res.string.month_9
            10 -> Res.string.month_10
            11 -> Res.string.month_11
            else -> Res.string.month_12
        },
    )
    return stringResource(Res.string.month_with_year, name, year)
}
