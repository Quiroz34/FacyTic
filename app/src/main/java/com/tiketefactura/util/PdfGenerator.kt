package com.tiketefactura.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tiketefactura.data.local.DocumentoConItems
import com.tiketefactura.data.local.Empresa

object PdfGenerator {

    fun generateAndPrint(context: Context, doc: DocumentoConItems, empresa: Empresa?) {
        val webView = WebView(context)
        val html = getHtml(doc, empresa)
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Documento_${doc.documento.numero}")
                val jobName = "TiketeFactura_${doc.documento.numero}"
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }

    private fun getHtml(doc: DocumentoConItems, empresa: Empresa?): String {
        val color = if (doc.documento.tipo == "FACTURA") "#6366F1" else "#EC4899"
        val isFactura = doc.documento.tipo == "FACTURA"
        
        return """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; padding: 40px; color: #1e293b; background: white; }
                    .header { text-align: center; margin-bottom: 40px; }
                    .brand { font-size: 28px; font-weight: 900; color: $color; text-transform: uppercase; margin-bottom: 5px; }
                    .slogan { font-size: 14px; color: #64748b; margin-top: 0; font-style: italic; }
                    
                    .doc-info { display: flex; justify-content: space-between; margin-bottom: 40px; padding: 20px; background: #f8fafc; border-radius: 12px; }
                    .info-block h3 { font-size: 12px; color: #94a3b8; text-transform: uppercase; margin-bottom: 8px; letter-spacing: 1px; }
                    .info-block p { font-size: 15px; font-weight: 600; margin: 0; color: #0f172a; }
                    
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th { text-align: left; font-size: 12px; color: #94a3b8; text-transform: uppercase; padding: 12px 10px; border-bottom: 2px solid #e2e8f0; }
                    td { padding: 15px 10px; border-bottom: 1px solid #f1f5f9; font-size: 14px; color: #334155; }
                    .qty { color: #94a3b8; font-weight: bold; }
                    
                    .totals { margin-top: 40px; float: right; width: 250px; }
                    .total-row { display: flex; justify-content: space-between; padding: 8px 0; }
                    .total-row.grand-total { margin-top: 15px; padding-top: 15px; border-top: 2px solid #e2e8f0; font-size: 20px; font-weight: 900; color: $color; }
                    
                    .footer { margin-top: 100px; text-align: center; color: #94a3b8; font-size: 12px; border-top: 1px dashed #e2e8f0; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="brand">${empresa?.nombre ?: "MI NEGOCIO"}</div>
                    ${if (!empresa?.eslogan.isNullOrBlank()) "<p class=\"slogan\">${empresa?.eslogan}</p>" else ""}
                    <div style="font-size: 12px; color: #64748b; margin-top: 10px;">
                        ${empresa?.ruc?.let { "RUC/NIT: $it | " } ?: ""}
                        ${empresa?.telefono?.let { "Tel: $it" } ?: ""}
                        <br>${empresa?.direccion ?: ""}
                    </div>
                </div>

                <div class="doc-info">
                    <div class="info-block">
                        <h3>Cliente</h3>
                        <p>${doc.documento.clienteNombre}</p>
                    </div>
                    <div class="info-block" style="text-align: right">
                        <h3>${doc.documento.tipo}</h3>
                        <p># ${doc.documento.numero}</p>
                        <p style="font-size: 11px; font-weight: normal; color: #64748b;">${doc.documento.fecha}</p>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th width="10%">Cant.</th>
                            <th>Descripción</th>
                            <th width="20%" style="text-align: right">Precio</th>
                            <th width="20%" style="text-align: right">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${doc.items.joinToString("") { """
                            <tr>
                                <td class="qty">${it.cantidad}</td>
                                <td style="font-weight: 600;">${it.nombre}</td>
                                <td style="text-align: right;">$${String.format("%.2f", it.precio)}</td>
                                <td style="text-align: right; font-weight: 700;">$${String.format("%.2f", it.subtotal)}</td>
                            </tr>
                        """ }}
                    </tbody>
                </table>

                <div class="totals">
                    <div class="total-row">
                        <span style="color: #94a3b8">Subtotal</span>
                        <span>$${String.format("%.2f", doc.documento.subtotal)}</span>
                    </div>
                    ${if (isFactura) """
                        <div class="total-row">
                            <span style="color: #94a3b8">IVA (15%)</span>
                            <span>$${String.format("%.2f", doc.documento.impuesto)}</span>
                        </div>
                    """ else ""}
                    <div class="total-row grand-total">
                        <span>TOTAL</span>
                        <span>$${String.format("%.2f", doc.documento.total)}</span>
                    </div>
                </div>

                <div style="clear: both;"></div>

                <div class="footer">
                    <p>${empresa?.mensajePie ?: "¡Gracias por su preferencia!"}</p>
                    <p style="font-size: 10px; opacity: 0.5;">Generado por Tikete & Factura Premium</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
