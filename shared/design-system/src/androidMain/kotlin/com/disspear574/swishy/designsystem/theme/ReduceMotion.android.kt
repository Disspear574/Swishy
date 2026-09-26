package com.disspear574.swishy.designsystem.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun isReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        // Android has no dedicated flag; a zero animator duration scale means reduced motion.
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}
