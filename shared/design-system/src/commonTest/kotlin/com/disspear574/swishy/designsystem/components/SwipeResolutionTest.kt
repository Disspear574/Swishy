package com.disspear574.swishy.designsystem.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SwipeResolutionTest {

    @Test
    fun `вправо — оставить, влево — в корзину`() {
        assertEquals(SwipeVerdict.Keep, resolveSwipe(horizontal = 1.2f, up = 0f, moveEnabled = true))
        assertEquals(SwipeVerdict.Trash, resolveSwipe(horizontal = -1f, up = 0f, moveEnabled = true))
    }

    @Test
    fun `до порога решения нет`() {
        assertNull(resolveSwipe(horizontal = 0.9f, up = 0.9f, moveEnabled = true))
    }

    @Test
    fun `вверх — в альбом, и при равных долях вверх побеждает`() {
        assertEquals(SwipeVerdict.Move, resolveSwipe(horizontal = 0.3f, up = 1.5f, moveEnabled = true))
        assertEquals(SwipeVerdict.Move, resolveSwipe(horizontal = 1.2f, up = 1.2f, moveEnabled = true))
    }

    @Test
    fun `сильный бок побеждает слабый верх`() {
        assertEquals(SwipeVerdict.Keep, resolveSwipe(horizontal = 1.6f, up = 1.1f, moveEnabled = true))
    }

    @Test
    fun `без альбомов жест вверх ничего не решает`() {
        assertNull(resolveSwipe(horizontal = 0f, up = 2f, moveEnabled = false))
        assertEquals(SwipeHint.None, swipeHint(horizontal = 0f, up = 2f, moveEnabled = false))
    }

    @Test
    fun `вниз решения нет`() {
        assertNull(resolveSwipe(horizontal = 0f, up = -3f, moveEnabled = true))
        assertEquals(SwipeHint.None, swipeHint(horizontal = 0f, up = -3f, moveEnabled = true))
    }

    @Test
    fun `подсказка называет направление и силу до единицы`() {
        val hint = swipeHint(horizontal = 0.5f, up = 0f, moveEnabled = true)
        assertEquals(SwipeVerdict.Keep, hint.verdict)
        assertEquals(0.5f, hint.magnitude)

        val capped = swipeHint(horizontal = 0f, up = 4f, moveEnabled = true)
        assertEquals(SwipeVerdict.Move, capped.verdict)
        assertEquals(1f, capped.magnitude)
    }
}
