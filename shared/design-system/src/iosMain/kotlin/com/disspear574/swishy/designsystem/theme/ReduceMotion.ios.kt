package com.disspear574.swishy.designsystem.theme

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
actual fun isReduceMotionEnabled(): Boolean = UIAccessibilityIsReduceMotionEnabled()
