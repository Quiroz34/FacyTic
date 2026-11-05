package com.tiketefactura.util

/** GS v 0 raster data, MSB is the leftmost pixel. Independent of Android for byte-level tests. */
object EscPosRaster {
    fun encode(width: Int, height: Int, black: BooleanArray): ByteArray {
        require(width in 1..576 && height in 1..256 && black.size == width * height)
        val bytesPerRow = (width + 7) / 8
        val output = ByteArray(8 + bytesPerRow * height)
        byteArrayOf(0x1d, 0x76, 0x30, 0, (bytesPerRow and 255).toByte(), (bytesPerRow shr 8).toByte(), (height and 255).toByte(), (height shr 8).toByte()).copyInto(output)
        for (y in 0 until height) for (x in 0 until width) if (black[y * width + x]) {
            val index = 8 + y * bytesPerRow + x / 8
            output[index] = (output[index].toInt() or (0x80 shr (x % 8))).toByte()
        }
        return output
    }
}
