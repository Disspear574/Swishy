package com.disspear574.swishy.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SwipeResolutionTest {

    @Test
    fun `right keeps and left trashes`() {
        assertEquals(SwipeVerdict.Keep, resolveSwipe(horizontal = 1.2f, up = 0f, moveEnabled = true))
        assertEquals(SwipeVerdict.Trash, resolveSwipe(horizontal = -1f, up = 0f, moveEnabled = true))
    }

    @Test
    fun `no verdict below the threshold`() {
        assertNull(resolveSwipe(horizontal = 0.9f, up = 0.9f, moveEnabled = true))
    }

    @Test
    fun `up moves to an album and wins ties`() {
        assertEquals(SwipeVerdict.Move, resolveSwipe(horizontal = 0.3f, up = 1.5f, moveEnabled = true))
        assertEquals(SwipeVerdict.Move, resolveSwipe(horizontal = 1.2f, up = 1.2f, moveEnabled = true))
    }

    @Test
    fun `strong sideways beats weak upward`() {
        assertEquals(SwipeVerdict.Keep, resolveSwipe(horizontal = 1.6f, up = 1.1f, moveEnabled = true))
    }

    @Test
    fun `upward swipe decides nothing without albums`() {
        assertNull(resolveSwipe(horizontal = 0f, up = 2f, moveEnabled = false))
        assertEquals(SwipeHint.None, swipeHint(horizontal = 0f, up = 2f, moveEnabled = false))
    }

    @Test
    fun `downward swipe has no verdict`() {
        assertNull(resolveSwipe(horizontal = 0f, up = -3f, moveEnabled = true))
        assertEquals(SwipeHint.None, swipeHint(horizontal = 0f, up = -3f, moveEnabled = true))
    }

    @Test
    fun `hint gives direction and magnitude capped at one`() {
        val hint = swipeHint(horizontal = 0.5f, up = 0f, moveEnabled = true)
        assertEquals(SwipeVerdict.Keep, hint.verdict)
        assertEquals(0.5f, hint.magnitude)

        val capped = swipeHint(horizontal = 0f, up = 4f, moveEnabled = true)
        assertEquals(SwipeVerdict.Move, capped.verdict)
        assertEquals(1f, capped.magnitude)
    }

    @Test
    fun `a direction is armed only once its threshold is reached`() {
        assertEquals(null, armedVerdict(swipeHint(horizontal = 0.9f, up = 0f, moveEnabled = true)))
        assertEquals(SwipeVerdict.Keep, armedVerdict(swipeHint(horizontal = 1f, up = 0f, moveEnabled = true)))
        assertEquals(SwipeVerdict.Trash, armedVerdict(swipeHint(horizontal = -1.4f, up = 0f, moveEnabled = true)))
        assertEquals(SwipeVerdict.Move, armedVerdict(swipeHint(horizontal = 0f, up = 1.2f, moveEnabled = true)))
        assertEquals(null, armedVerdict(SwipeHint.None))
    }
}
