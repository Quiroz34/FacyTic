package com.tiketefactura.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.*
import android.print.*
import android.util.Base64
import com.tiketefactura.data.local.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfGenerator {
    fun money(v: Double) = "$" + String.format(Locale.US, "%,.2f", v)
    fun bitmap(data: String?): Bitmap? = runCatching {
        if (data.isNullOrBlank()) null else {
            val bytes = Base64.decode(data.substringAfter("base64,", data), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }.getOrNull()
    fun encode(bitmap: Bitmap): String {
        val output = java.io.ByteArrayOutputStream()
        val scale = minOf(1f, 800f / maxOf(bitmap.width, bitmap.height))
        val small = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt().coerceAtLeast(1), (bitmap.height * scale).toInt().coerceAtLeast(1), true)
        small.compress(Bitmap.CompressFormat.PNG, 100, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }
    private class Writer(private val width: Int) {
        val pdf = PdfDocument()
        private val thermal = width < 300
        private val height = if (width < 300) 1000 else 842
        private val margin = if (thermal) 10f else 36f
        private var page: PdfDocument.Page? = null
        private var y = margin
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var index = 0
        private fun start() {
            page?.let { pdf.finishPage(it) }
            page = pdf.startPage(PdfDocument.PageInfo.Builder(width, height, ++index).create()); y = margin
        }
        private fun room(h: Float) { if (page == null || y + h > height - margin) start() }
        fun text(value: String, bold: Boolean = false, size: Float = if (thermal) 12f else 11f) {
            paint.textSize = size; paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            value.split('\n').forEach { paragraph ->
                var remaining = paragraph
                if (remaining.isEmpty()) { room(size * 1.5f); y += size * 1.5f }
                while (remaining.isNotEmpty()) {
                    val fit = paint.breakText(remaining, true, width - 2 * margin, null).coerceAtLeast(1)
                    val cut = if (fit < remaining.length) remaining.lastIndexOf(' ', fit).takeIf { it > 0 } ?: fit else fit
                    room(size * 1.5f); page!!.canvas.drawText(remaining.take(cut), margin, y + size, paint)
                    y += size * 1.5f; remaining = remaining.drop(cut).trimStart()
                }
            }
        }
        fun centered(value: String, bold: Boolean = false, size: Float = if (thermal) 12f else 11f) {
            paint.textSize = size; paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            var remaining = value
            while (remaining.isNotEmpty()) {
                val fit = paint.breakText(remaining, true, width - 2 * margin, null).coerceAtLeast(1)
                val cut = if (fit < remaining.length) remaining.lastIndexOf(' ', fit).takeIf { it > 0 } ?: fit else fit
                val line = remaining.take(cut)
                room(size * 1.5f)
                page!!.canvas.drawText(line, (width - paint.measureText(line)) / 2f, y + size, paint)
                y += size * 1.5f; remaining = remaining.drop(cut).trimStart()
            }
        }
        fun row(left: String, right: String, bold: Boolean = false, size: Float = if (thermal) 12f else 11f) {
            paint.textSize = size; paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            val rightWidth = paint.measureText(right)
            val leftWidth = (width - 2 * margin - rightWidth - paint.measureText("  ")).coerceAtLeast(width * .3f)
            val fit = paint.breakText(left, true, leftWidth, null).coerceAtLeast(1)
            val shownLeft = if (fit < left.length) left.take((fit - 1).coerceAtLeast(1)).trimEnd() + "…" else left
            room(size * 1.6f)
            page!!.canvas.drawText(shownLeft, margin, y + size, paint)
            page!!.canvas.drawText(right, width - margin - rightWidth, y + size, paint)
            y += size * 1.6f
        }
        fun divider() {
            room(10f); paint.strokeWidth = if (thermal) 1.5f else 1f
            page!!.canvas.drawLine(margin, y + 4f, width - margin, y + 4f, paint); y += 10f
        }
        fun space(size: Float = 5f) { room(size); y += size }
        fun image(value: String?, maxHeight: Float) {
            val b = bitmap(value) ?: return
            val scale = minOf((width - margin * 2) / b.width, maxHeight / b.height)
            val h = b.height * scale; val w = b.width * scale
            room(h + 10); page!!.canvas.drawBitmap(b, null, RectF(margin, y, margin + w, y + h), paint); y += h + 10; b.recycle()
        }
        fun save(file: File): File { if (page == null) start(); pdf.finishPage(page!!); file.outputStream().use(pdf::writeTo); pdf.close(); return file }
    }
    fun create(context: Context, full: DocumentoConItems, current: Empresa?, paperOverride: String? = null): File {
        val d = full.documento
        val snapshot = d.empresaSnapshot?.let { runCatching { JSONObject(it) }.getOrNull() }
        fun field(key: String, fallback: String?) = if (snapshot == null) fallback.orEmpty() else if (snapshot.isNull(key)) "" else snapshot.optString(key)
        fun enabled(key: String, fallback: Boolean) = if (snapshot == null || !snapshot.has(key)) fallback else snapshot.optBoolean(key, fallback)
        val format = paperOverride ?: field("formatoPapel", current?.formatoPapel ?: "A4")
        val w = Writer(when (format) { "58" -> 164; "80" -> 226; else -> 595 })
        w.image(field("logoUri", current?.logoUri), 60f)
        w.centered(field("nombre", current?.nombre ?: "Mi Negocio"), true, 17f)
        field("eslogan", current?.eslogan).takeIf { it.isNotBlank() }?.let { w.centered(it) }
        listOf(field("ruc", current?.ruc), field("direccion", current?.direccion)).filter { it.isNotBlank() }.forEach { w.centered(it) }
        if (enabled("mostrarContactoTicket", current?.mostrarContactoTicket ?: true)) {
            field("telefono", current?.telefono).takeIf { it.isNotBlank() }?.let { w.centered("Contacto: $it") }
        }
        if (enabled("mostrarEmergenciaTicket", current?.mostrarEmergenciaTicket ?: false)) {
            field("telefonoEmergencia", current?.telefonoEmergencia).takeIf { it.isNotBlank() }?.let { w.centered("Emergencias: $it", true) }
        }
        w.divider()
        w.centered(d.tipo, true, 15f)
        w.centered(d.numero, true, 14f)
        w.centered(d.fecha)
        w.space()
        w.text("DATOS DE LA VENTA", true)
        w.text("Cliente: ${d.clienteNombre}")
        w.text("Forma de pago: ${d.metodoPago}")
        w.text("Estado: ${d.estado}", true)
        d.motivoAnulacion?.let { w.text("ANULADA: $it", true) }
        w.divider()
        w.text("PRODUCTOS", true)
        fun unitName(unit: String) = when (unit) { "KG" -> "kg"; "L" -> "L"; else -> "pieza" }
        fun quantity(value: Double) = java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
        full.items.forEach { i ->
            w.text(i.nombre, true)
            w.row("${quantity(i.cantidad)} ${unitName(i.unidad)} x ${money(i.precio)}", money(i.subtotal))
        }
        w.divider()
        w.text("RESUMEN", true)
        w.row("Subtotal", money(d.subtotal))
        if (d.descuento > 0) w.row("Descuento", "-${money(d.descuento)}")
        if (d.impuesto > 0) w.row("Impuesto (${d.impuestoPorcentaje}%)", money(d.impuesto))
        w.divider()
        w.row("TOTAL", money(d.total), true, 16f)
        if (d.metodoPago == "EFECTIVO") {
            w.row("Efectivo recibido", money(d.efectivoRecibido))
            w.row("CAMBIO", money((d.efectivoRecibido - d.total).coerceAtLeast(0.0)), true, 14f)
        }
        if (d.metodoPago == "CRÉDITO") {
            w.row("Abonado", money(d.abonado))
            w.row("SALDO PENDIENTE", money(if (d.estado == "ANULADA") 0.0 else d.total - d.abonado), true, 14f)
        }
        if (!d.firma.isNullOrBlank()) { w.text("Firma del cliente"); w.image(d.firma, 100f) }
        field("mensajePie", current?.mensajePie).takeIf { it.isNotBlank() }?.let { w.space(8f); w.centered(it, true) }
        return w.save(File(context.cacheDir, "${d.numero}${paperOverride?.let { "-${it}mm" }.orEmpty()}.pdf"))
    }
    fun receipt(context: Context, client: Cliente, payments: List<Abono>): File {
        val w = Writer(595)
        w.text("RECIBO DE ABONO", true, 18f); w.text("Cliente: ${client.nombre}")
        payments.forEach { a -> w.text("Abono #${a.id} · ${a.fecha} · ${a.metodo}"); w.text("${money(a.monto)} · Comprobante: ${a.documentoId?.toString() ?: "saldo anterior"}") }
        w.text("Total recibido: ${money(payments.sumOf { it.monto })}", true)
        return w.save(File(context.cacheDir, "Abono-${payments.first().id}.pdf"))
    }
    fun generateAndPrint(context: Context, doc: DocumentoConItems, empresa: Empresa?) = print(context, create(context, doc, empresa))
    fun print(context: Context, file: File) {
        val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        manager.print(file.nameWithoutExtension, object : PrintDocumentAdapter() {
            override fun onLayout(old: PrintAttributes?, new: PrintAttributes?, signal: CancellationSignal?, result: LayoutResultCallback?, extras: Bundle?) {
                if (signal?.isCanceled == true) { result?.onLayoutCancelled(); return }
                try {
                    val pages = android.graphics.pdf.PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { it.pageCount }
                    result?.onLayoutFinished(PrintDocumentInfo.Builder(file.name).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).setPageCount(pages).build(), true)
                } catch (e: Exception) { result?.onLayoutFailed(e.message) }
            }
            override fun onWrite(pages: Array<out PageRange>?, destination: ParcelFileDescriptor?, signal: CancellationSignal?, result: WriteResultCallback?) {
                try {
                    if (signal?.isCanceled == true) { result?.onWriteCancelled(); return }
                    val ranges = pages ?: arrayOf(PageRange.ALL_PAGES)
                    FileOutputStream(destination!!.fileDescriptor).use { output ->
                        if (ranges.any { it == PageRange.ALL_PAGES }) file.inputStream().use { it.copyTo(output) }
                        else android.graphics.pdf.PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use { source ->
                            val selected = PdfDocument()
                            try {
                                for (i in 0 until source.pageCount) {
                                    if (ranges.none { i in it.start..it.end }) continue
                                    if (signal?.isCanceled == true) { result?.onWriteCancelled(); return }
                                    source.openPage(i).use { original ->
                                        val bitmap = Bitmap.createBitmap(original.width * 2, original.height * 2, Bitmap.Config.ARGB_8888)
                                        bitmap.eraseColor(Color.WHITE)
                                        original.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                                        val page = selected.startPage(PdfDocument.PageInfo.Builder(original.width, original.height, i + 1).create())
                                        page.canvas.drawBitmap(bitmap, null, Rect(0, 0, original.width, original.height), null)
                                        selected.finishPage(page); bitmap.recycle()
                                    }
                                }
                                selected.writeTo(output)
                            } finally { selected.close() }
                        }
                    }
                    result?.onWriteFinished(ranges)
                } catch (e: Exception) { result?.onWriteFailed(e.message) }
            }
        }, null)
    }
}
