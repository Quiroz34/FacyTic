package com.tiketefactura.util

import org.junit.Assert.*
import org.junit.Test

class EscPosRasterTest {
    @Test fun encodesHeaderAndMostSignificantPixelFirst() {
        val pixels = BooleanArray(16); pixels[0] = true; pixels[7] = true; pixels[9] = true
        val result = EscPosRaster.encode(8, 2, pixels)
        assertArrayEquals(byteArrayOf(0x1d, 0x76, 0x30, 0, 1, 0, 2, 0, 0x81.toByte(), 0x40), result)
    }
    @Test fun padsLastByteWithWhitePixels() {
        assertEquals(0x80, EscPosRaster.encode(9, 1, BooleanArray(9) { it == 8 }).last().toInt() and 255)
        assertThrows(IllegalArgumentException::class.java) { EscPosRaster.encode(384, 128, BooleanArray(1)) }
    }
}
