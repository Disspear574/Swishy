package com.disspear574.swishy.decisions

import kotlin.test.Test
import kotlin.test.assertEquals

class SizeTextTest {

    @Test
    fun `байты показываются целыми`() {
        assertEquals(SizeText("512", SizeUnit.BYTES), formatSize(512))
    }

    @Test
    fun `килобайты от 1024`() {
        assertEquals(SizeText("1", SizeUnit.KILOBYTES), formatSize(1_024))
    }

    @Test
    fun `мегабайты с одним знаком после запятой`() {
        assertEquals(SizeText("1,5", SizeUnit.MEGABYTES), formatSize(1_572_864))
    }

    @Test
    fun `гигабайты с одним знаком после запятой`() {
        assertEquals(SizeText("2,5", SizeUnit.GIGABYTES), formatSize(2_684_354_560))
    }

    @Test
    fun `круглое значение не показывает запятую`() {
        assertEquals(SizeText("3", SizeUnit.GIGABYTES), formatSize(3L * 1024 * 1024 * 1024))
    }

    @Test
    fun `ноль это ноль байт, а не пустая строка`() {
        assertEquals(SizeText("0", SizeUnit.BYTES), formatSize(0))
    }

    @Test
    fun `отрицательный размер невозможен и сводится к нулю`() {
        assertEquals(SizeText("0", SizeUnit.BYTES), formatSize(-1))
    }
}
