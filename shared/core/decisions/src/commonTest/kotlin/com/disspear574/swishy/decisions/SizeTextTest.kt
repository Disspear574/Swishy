package com.disspear574.swishy.decisions

import kotlin.test.Test
import kotlin.test.assertEquals

class SizeTextTest {

    @Test
    fun `bytes are shown as whole numbers`() {
        assertEquals(SizeText("512", SizeUnit.BYTES), formatSize(512))
    }

    @Test
    fun `kilobytes start at 1024`() {
        assertEquals(SizeText("1", SizeUnit.KILOBYTES), formatSize(1_024))
    }

    @Test
    fun `megabytes have one decimal digit`() {
        assertEquals(SizeText("1,5", SizeUnit.MEGABYTES), formatSize(1_572_864))
    }

    @Test
    fun `gigabytes have one decimal digit`() {
        assertEquals(SizeText("2,5", SizeUnit.GIGABYTES), formatSize(2_684_354_560))
    }

    @Test
    fun `round value has no decimal separator`() {
        assertEquals(SizeText("3", SizeUnit.GIGABYTES), formatSize(3L * 1024 * 1024 * 1024))
    }

    @Test
    fun `zero is zero bytes, not an empty string`() {
        assertEquals(SizeText("0", SizeUnit.BYTES), formatSize(0))
    }

    @Test
    fun `negative size is clamped to zero`() {
        assertEquals(SizeText("0", SizeUnit.BYTES), formatSize(-1))
    }
}
