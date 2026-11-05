package com.tiketefactura.util

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class PairedPrinter(val name: String, val address: String, val classic: Boolean)

object BluetoothPrinter {
    private val spp = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    fun permitted(context: Context) = Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    @SuppressLint("MissingPermission")
    fun paired(context: Context): List<PairedPrinter> {
        require(permitted(context)) { "Permite el acceso a dispositivos cercanos" }
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter ?: error("Este dispositivo no tiene Bluetooth")
        require(adapter.isEnabled) { "Activa Bluetooth y vincula la impresora desde Ajustes del teléfono" }
        return adapter.bondedDevices.map { PairedPrinter(it.name ?: "Dispositivo Bluetooth", it.address, it.type != BluetoothDevice.DEVICE_TYPE_LE) }.sortedBy { it.name }
    }

    @SuppressLint("MissingPermission")
    suspend fun print(context: Context, address: String, pdf: File, dots: Int, insecure: Boolean = false) = withContext(Dispatchers.IO) {
        require(dots == 384 || dots == 576) { "Selecciona papel de 58 o 80 mm" }
        require(permitted(context)) { "Permite el acceso a dispositivos cercanos" }
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter ?: error("Bluetooth no disponible")
        require(adapter.isEnabled) { "Activa Bluetooth" }
        val device = adapter.bondedDevices.find { it.address == address } ?: error("Vincula la impresora en los ajustes Bluetooth del teléfono")
        require(device.type != BluetoothDevice.DEVICE_TYPE_LE) { "Esta impresora usa BLE. Se necesita confirmar su modelo y protocolo del fabricante" }
        // No hidden APIs, discovery, location permissions, or assumptions about BLE characteristics.
        val socket = if (insecure) device.createInsecureRfcommSocketToServiceRecord(spp) else device.createRfcommSocketToServiceRecord(spp)
        val watchdog = Executors.newSingleThreadScheduledExecutor()
        val timeout = watchdog.schedule({ runCatching { socket.close() } }, 25, TimeUnit.SECONDS)
        try {
            socket.connect(); timeout.cancel(false)
            val output = socket.outputStream
            output.write(byteArrayOf(0x1b, 0x40))
            val writeTimeout = watchdog.schedule({ runCatching { socket.close() } }, 120, TimeUnit.SECONDS)
            try {
                PdfRenderer(ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                    for (pageIndex in 0 until renderer.pageCount) renderer.openPage(pageIndex).use { page ->
                        val scale = dots.toFloat() / page.width
                        val pageHeight = kotlin.math.ceil(page.height * scale).toInt()
                        val image = Bitmap.createBitmap(dots, pageHeight, Bitmap.Config.ARGB_8888)
                        try {
                            image.eraseColor(Color.WHITE)
                            val matrix = Matrix().apply { setScale(scale, scale) }
                            page.render(image, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            val pixels = IntArray(dots * pageHeight); image.getPixels(pixels, 0, dots, 0, 0, dots, pageHeight)
                            fun black(p: Int) = (Color.red(p) * 299 + Color.green(p) * 587 + Color.blue(p) * 114) < 160_000
                            // Trim the blank PDF page tail instead of feeding a full sheet of thermal paper.
                            val lastPixel = pixels.indexOfLast { black(it) }
                            val lastRow = if (lastPixel < 0) 0 else minOf(pageHeight, lastPixel / dots + 12)
                            var top = 0
                            while (top < lastRow) {
                                ensureActive()
                                val rows = minOf(128, lastRow - top)
                                val mask = BooleanArray(dots * rows) { i -> black(pixels[top * dots + i]) }
                                val bytes = EscPosRaster.encode(dots, rows, mask)
                                // Small writes and pacing accommodate the limited buffers of pocket printers.
                                var offset = 0
                                while (offset < bytes.size) { ensureActive(); val count = minOf(512, bytes.size - offset); output.write(bytes, offset, count); offset += count; delay(12) }
                                output.flush(); delay(35)
                                top += rows
                            }
                        } finally { image.recycle() }
                    }
                }
                output.write(byteArrayOf(0x0a, 0x0a, 0x0a)); output.flush(); delay(250)
            } finally { writeTimeout.cancel(false) }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { throw IllegalStateException("No se completó el envío. Revisa conexión, papel y compatibilidad ESC/POS. Puede haberse impreso una parte; compruébalo antes de reintentar.", e) }
        finally { runCatching { socket.close() }; watchdog.shutdownNow() }
    }
}
