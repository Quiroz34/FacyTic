package com.tiketefactura.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.tiketefactura.data.BusinessRules
import com.tiketefactura.data.local.Cliente
import com.tiketefactura.data.local.DocumentoConItems
import java.io.File

object Sharing {
    private fun uri(context: Context, file: File) = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    private fun message(doc: DocumentoConItems) = doc.documento.let { d ->
        "Hola ${d.clienteNombre.orEmpty()}. Gracias por su compra. Adjuntamos su ${d.tipo.lowercase()} ${d.numero} por ${PdfGenerator.money(d.total)}."
    }
    fun file(context: Context, file: File, mime: String) {
        val uri = uri(context, file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = mime; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri(file.name, uri)
        }, "Compartir ${file.name}"))
    }
    fun whatsapp(context: Context, file: File, doc: DocumentoConItems, client: Cliente?) {
        val content = uri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TEXT, message(doc))
            putExtra(Intent.EXTRA_STREAM, content)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri(file.name, content)
        }
        val opened = listOf("com.whatsapp", "com.whatsapp.w4b").any { packageName ->
            runCatching { context.startActivity(Intent(intent).setPackage(packageName)); true }.getOrDefault(false)
        }
        if (!opened) throw IllegalStateException("No se encontró WhatsApp. Usa Compartir PDF para elegir otra aplicación.")
    }
    fun email(context: Context, file: File, doc: DocumentoConItems, client: Cliente?) {
        val content = uri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            client?.correo?.takeIf { it.isNotBlank() }?.let { putExtra(Intent.EXTRA_EMAIL, arrayOf(it)) }
            putExtra(Intent.EXTRA_SUBJECT, "${doc.documento.tipo.lowercase().replaceFirstChar { it.uppercase() }} ${doc.documento.numero}")
            putExtra(Intent.EXTRA_TEXT, message(doc))
            putExtra(Intent.EXTRA_STREAM, content)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri(file.name, content)
        }
        context.startActivity(Intent.createChooser(intent, "Enviar comprobante por correo"))
    }
    fun csv(docs: List<DocumentoConItems>): String = "\uFEFF" +
        (listOf(listOf("Número", "Tipo", "Fecha", "Cliente", "Subtotal", "Descuento", "Impuesto", "Total", "Pago", "Estado", "Abonado")) +
            docs.map { it.documento.let { d -> listOf(d.numero, d.tipo, d.fecha, d.clienteNombre ?: "", d.subtotal, d.descuento, d.impuesto, d.total, d.metodoPago, d.estado, d.abonado) } })
            .joinToString("\r\n") { row -> row.joinToString(",") { cell ->
                val value = cell.toString()
                BusinessRules.csvCell(if (cell is String && value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'$value" else value)
            } }
}
