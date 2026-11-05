package com.tiketefactura.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.tiketefactura.data.BusinessRules
import com.tiketefactura.data.local.DocumentoConItems
import java.io.File

object Sharing {
    fun file(context: Context, file: File, mime: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = mime; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newRawUri(file.name, uri)
        }, "Compartir ${file.name}"))
    }
    fun csv(docs: List<DocumentoConItems>): String = "\uFEFF" +
        (listOf(listOf("Número", "Tipo", "Fecha", "Cliente", "Subtotal", "Descuento", "Impuesto", "Total", "Pago", "Estado", "Abonado")) +
            docs.map { it.documento.let { d -> listOf(d.numero, d.tipo, d.fecha, d.clienteNombre ?: "", d.subtotal, d.descuento, d.impuesto, d.total, d.metodoPago, d.estado, d.abonado) } })
            .joinToString("\r\n") { row -> row.joinToString(",") { cell ->
                val value = cell.toString()
                BusinessRules.csvCell(if (cell is String && value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'$value" else value)
            } }
}
