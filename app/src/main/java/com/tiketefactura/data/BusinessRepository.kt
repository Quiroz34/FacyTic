package com.tiketefactura.data

import androidx.room.withTransaction
import com.tiketefactura.data.local.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class SaleLine(val productId: Int, val quantity: Double)

@Singleton
class BusinessRepository @Inject constructor(val db: AppDatabase) {
    private val rules = BusinessRules
    private fun positive(value: Double, label: String, zero: Boolean = false) {
        require(value.isFinite() && if (zero) value >= 0 else value > 0) { "$label inválido" }
    }

    suspend fun sale(lines: List<SaleLine>, clientName: String, clientId: Int?, type: String,
                     method: String, discount: Double, applyTax: Boolean, received: Double, signature: String?): DocumentoConItems = db.withTransaction {
        require(type in listOf("TICKET", "FACTURA") && method in METHODS) { "Tipo de venta inválido" }
        require(lines.map { it.productId }.distinct().size == lines.size) { "Hay productos repetidos" }
        val company = db.empresaDao().get().first() ?: Empresa()
        val products = db.productoDao().getAll().first().associateBy { it.id }
        val selected = lines.map { line ->
            val p = requireNotNull(products[line.productId]) { "El producto ya no existe" }
            positive(line.quantity, "Cantidad")
            require(p.unidad != UNIT_PIECE || line.quantity == kotlin.math.floor(line.quantity)) { "${p.nombre} se vende por pieza; usa cantidades completas" }
            require(company.permitirSinStock || line.quantity <= p.stock) { "Stock insuficiente: ${p.nombre}" }
            p to line.quantity
        }
        val totals = rules.totals(selected.map { it.first.precio to it.second }, discount, if (applyTax) company.impuestoPorcentaje else 0.0)
        val client = db.clienteDao().getAll().first().find { it.id == clientId }
        require(clientId == null || client != null) { "El cliente ya no existe" }
        require(method != "CRÉDITO" || client != null) { "Selecciona un cliente registrado para vender a crédito" }
        if (method == "EFECTIVO") require(received.isFinite() && rules.amount(received) >= totals.total) { "El efectivo recibido no cubre el total" }
        val box = db.operacionesDao().cajaAbierta()
        val timestamp = rules.now()
        val snapshot = JSONObject().apply {
            put("nombre", company.nombre); put("ruc", company.ruc); put("direccion", company.direccion)
            put("telefono", company.telefono); put("eslogan", company.eslogan); put("mensajePie", company.mensajePie)
            put("logoUri", company.logoUri); put("formatoPapel", company.formatoPapel)
        }.toString()
        val id = db.documentoDao().insertDocumento(Documento(tipo = type, numero = "BORRADOR", fecha = timestamp,
            clienteNombre = client?.nombre ?: clientName.ifBlank { "Consumidor Final" }, clienteId = client?.id,
            subtotal = totals.subtotal, descuento = totals.discount, impuesto = totals.tax, total = totals.total,
            estado = if (method == "CRÉDITO" && totals.total > 0) "PENDIENTE" else "PAGADA", metodoPago = method,
            impuestoPorcentaje = if (applyTax) company.impuestoPorcentaje else 0.0, firma = signature,
            abonado = if (method == "CRÉDITO") 0.0 else totals.total, efectivoRecibido = if (method == "EFECTIVO") rules.amount(received) else 0.0,
            costoTotal = rules.amount(selected.sumOf { rules.lineTotal(it.first.costo, it.second) }), cajaId = box?.id, empresaSnapshot = snapshot)).toInt()
        val saved = requireNotNull(db.documentoDao().find(id)).documento.copy(numero = "${if (type == "FACTURA") "F" else "T"}-${id.toString().padStart(8, '0')}")
        db.documentoDao().update(saved)
        db.documentoDao().insertItems(selected.map { (p, qty) -> DocumentoItem(documentoId = id, nombre = p.nombre,
            cantidad = qty, precio = p.precio, subtotal = rules.lineTotal(p.precio, qty), productoId = p.id, costo = p.costo, unidad = p.unidad) })
        selected.forEach { (p, qty) ->
            db.productoDao().update(p.copy(stock = p.stock - qty))
            db.inventarioDao().insert(InventarioMovimiento(productoId = p.id, productoNombre = p.nombre, cantidad = -qty, tipo = "VENTA ${saved.numero}", fecha = timestamp))
        }
        if (method == "CRÉDITO" && client != null) db.clienteDao().update(client.copy(deuda = rules.amount(client.deuda + totals.total)))
        requireNotNull(db.documentoDao().find(id))
    }

    suspend fun product(p: Producto) = db.withTransaction {
        require(p.nombre.isNotBlank()) { "Escribe el nombre del producto" }
        require(p.unidad in UNITS) { "Selecciona una unidad de venta válida" }
        positive(p.precio, "Precio", true); positive(p.costo, "Costo", true); positive(p.stockMinimo, "Stock mínimo", true)
        require(p.stock.isFinite()) { "Stock inválido" }
        val all = db.productoDao().getAll().first()
        require(all.none { it.id != p.id && rules.key(it.nombre) == rules.key(p.nombre) }) { "Ya existe un producto con ese nombre; distingue la presentación o variante" }
        val barcode = p.codigoBarras?.trim()?.ifBlank { null }
        require(barcode == null || all.none { it.id != p.id && it.codigoBarras?.trim() == barcode }) { "El código de barras ya está registrado" }
        require(p.unidad != UNIT_PIECE || p.stock == kotlin.math.floor(p.stock)) { "El stock de piezas debe ser un número entero" }
        require(p.unidad != UNIT_PIECE || p.stockMinimo == kotlin.math.floor(p.stockMinimo)) { "El stock mínimo de piezas debe ser un número entero" }
        val cleaned = p.copy(nombre = p.nombre.trim(), codigoBarras = barcode, proveedor = p.proveedor.trim(), precio = rules.amount(p.precio), costo = rules.amount(p.costo))
        if (p.id == 0) {
            require(p.stock >= 0) { "El stock inicial no puede ser negativo" }
            db.productoDao().insert(cleaned)
            val added = db.productoDao().getAll().first().maxBy { it.id }
            if (p.stock != 0.0) movement(added, p.stock, "ENTRADA INICIAL")
        } else {
            val old = requireNotNull(all.find { it.id == p.id }) { "Producto inexistente" }
            // Stock is changed through the inventory screen, using a fresh value inside the transaction.
            db.productoDao().update(cleaned.copy(stock = old.stock))
        }
    }

    private suspend fun movement(p: Producto, qty: Double, type: String) {
        db.inventarioDao().insert(InventarioMovimiento(productoId = p.id, productoNombre = p.nombre, cantidad = qty, tipo = type, fecha = rules.now()))
    }

    suspend fun adjust(productId: Int, quantity: Double, reason: String) = db.withTransaction {
        require(quantity.isFinite() && quantity != 0.0 && reason.isNotBlank()) { "Indica cantidad y motivo del movimiento" }
        val p = requireNotNull(db.productoDao().getAll().first().find { it.id == productId }) { "Producto inexistente" }
        require(p.unidad != UNIT_PIECE || quantity == kotlin.math.floor(quantity)) { "${p.nombre} se controla por piezas completas" }
        require(p.stock + quantity >= 0) { "El movimiento dejaría inventario negativo" }
        db.productoDao().update(p.copy(stock = p.stock + quantity))
        movement(p, quantity, "${if (quantity > 0) "ENTRADA" else "AJUSTE"}: $reason")
    }

    suspend fun client(c: Cliente) = db.withTransaction {
        rules.validateClient(c.nombre, c.telefono, c.correo)
        val all = db.clienteDao().getAll().first()
        val phone = rules.phoneKey(c.telefono); val email = rules.key(c.correo.orEmpty())
        require(all.none { it.id != c.id && (
            rules.key(it.nombre) == rules.key(c.nombre) && rules.phoneKey(it.telefono) == phone ||
            phone.isNotBlank() && rules.phoneKey(it.telefono) == phone || email.isNotBlank() && rules.key(it.correo.orEmpty()) == email)
        }) { "Ya existe un cliente con ese nombre y teléfono, teléfono o correo" }
        val cleaned = c.copy(nombre = c.nombre.trim(), telefono = c.telefono?.trim()?.ifBlank { null }, correo = c.correo?.trim()?.ifBlank { null }, direccion = c.direccion?.trim()?.ifBlank { null })
        if (c.id == 0) db.clienteDao().insert(cleaned.copy(deuda = 0.0)) else {
            val old = requireNotNull(all.find { it.id == c.id }) { "Cliente inexistente" }
            db.clienteDao().update(cleaned.copy(deuda = old.deuda))
        }
    }

    suspend fun category(c: Categoria) = db.withTransaction {
        require(c.nombre.isNotBlank()) { "Escribe el nombre de la categoría" }
        val all = db.categoriaDao().getAll().first()
        require(all.none { it.id != c.id && rules.key(it.nombre) == rules.key(c.nombre) }) { "La categoría ya existe" }
        if (c.id == 0) db.categoriaDao().insert(c.copy(nombre = c.nombre.trim())) else {
            require(all.any { it.id == c.id }) { "La categoría ya no existe" }
            db.categoriaDao().update(c.copy(nombre = c.nombre.trim()))
        }
    }

    suspend fun payment(clientId: Int, amount: Double, method: String, documentId: Int? = null): List<Abono> = db.withTransaction {
        require(method in METHODS && method != "CRÉDITO") { "Método de pago inválido" }
        val client = requireNotNull(db.clienteDao().getAll().first().find { it.id == clientId }) { "Cliente inexistente" }
        val balance = rules.payment(client.deuda, amount)
        var remaining = rules.amount(amount)
        val pending = db.documentoDao().getAllWithItems().first().map { it.documento }
            .filter { it.clienteId == clientId && it.estado != "ANULADA" && it.metodoPago == "CRÉDITO" && it.total > it.abonado }
            .sortedBy { it.id }
        val records = mutableListOf<Abono>()
        val box = db.operacionesDao().cajaAbierta()
        suspend fun record(docId: Int?, value: Double) {
            val a = Abono(clienteId = clientId, documentoId = docId, monto = value, fecha = rules.now(), metodo = method, cajaId = box?.id)
            records += a.copy(id = db.operacionesDao().insertAbono(a).toInt())
        }
        val targets = if (documentId != null) pending.filter { it.id == documentId } else pending
        if (documentId != null) require(targets.size == 1 && remaining <= rules.amount(targets.first().total - targets.first().abonado)) { "El abono supera el saldo del comprobante" }
        for (d in targets) {
            if (remaining <= 0) break
            val applied = minOf(remaining, rules.amount(d.total - d.abonado))
            val paid = rules.amount(d.abonado + applied)
            db.documentoDao().update(d.copy(abonado = paid, estado = if (paid >= d.total) "PAGADA" else "PENDIENTE"))
            record(d.id, applied); remaining = rules.amount(remaining - applied)
        }
        // A legacy balance can exist without matching documents. Its receipt remains explicit.
        if (remaining > 0) record(null, remaining)
        db.clienteDao().update(client.copy(deuda = balance))
        records
    }

    suspend fun cancel(id: Int, reason: String) = db.withTransaction {
        require(reason.isNotBlank()) { "Escribe el motivo de cancelación o devolución" }
        val full = requireNotNull(db.documentoDao().find(id)) { "Venta inexistente" }
        val d = full.documento
        require(d.estado != "ANULADA") { "La venta ya fue anulada" }
        require(full.items.all { it.productoId != null }) { "Esta venta antigua no identifica los productos; requiere conciliación manual" }
        val box = db.operacionesDao().cajaAbierta()
        val refunds = if (d.metodoPago == "CRÉDITO") {
            val records = db.operacionesDao().abonos().first().filter { it.documentoId == d.id }
            require(rules.amount(records.sumOf { it.monto }) == rules.amount(d.abonado)) { "Hay abonos antiguos sin método conocido; concilia ese saldo antes de devolver" }
            records.groupBy { it.metodo }.mapValues { rules.amount(it.value.sumOf { a -> a.monto }) }
        } else mapOf(d.metodoPago to d.total)
        require(refunds["EFECTIVO"].let { it == null || it == 0.0 || box != null }) { "Abre una caja para registrar el reembolso en efectivo" }
        val products = db.productoDao().getAll().first().associateBy { it.id }
        full.items.forEach { item ->
            val p = requireNotNull(products[item.productoId]) { "No se puede restituir un producto eliminado" }
            db.productoDao().update(p.copy(stock = p.stock + item.cantidad)); movement(p, item.cantidad, "DEVOLUCIÓN ${d.numero}: $reason")
        }
        if (d.metodoPago == "CRÉDITO") {
            val c = requireNotNull(db.clienteDao().getAll().first().find { it.id == d.clienteId }) { "Cliente inexistente" }
            db.clienteDao().update(c.copy(deuda = rules.amount((c.deuda - (d.total - d.abonado)).coerceAtLeast(0.0))))
        }
        refunds.filterValues { it > 0 }.forEach { (method, value) ->
            db.operacionesDao().insertReembolso(Reembolso(documentoId = d.id, monto = value, metodo = method, fecha = rules.now(), cajaId = box?.id, motivo = reason))
        }
        db.documentoDao().update(d.copy(estado = "ANULADA", motivoAnulacion = reason))
    }

    suspend fun deleteProduct(p: Producto) = db.withTransaction {
        require(db.documentoDao().getAllWithItems().first().none { doc -> doc.items.any { it.productoId == p.id } }) { "El producto tiene ventas; conserva su historial" }
        db.productoDao().delete(p)
    }
    suspend fun deleteClient(c: Cliente) = db.withTransaction {
        val fresh = requireNotNull(db.clienteDao().getAll().first().find { it.id == c.id })
        require(fresh.deuda == 0.0 && db.documentoDao().getAllWithItems().first().none { it.documento.clienteId == c.id } && db.operacionesDao().abonos().first().none { it.clienteId == c.id }) { "El cliente tiene deuda o historial; no se puede eliminar" }
        db.clienteDao().delete(fresh)
    }
    suspend fun expense(description: String, value: Double, category: String, method: String, id: Int = 0) = db.withTransaction {
        require(description.isNotBlank()) { "Escribe la descripción" }; positive(rules.amount(value), "Monto")
        require(method in METHODS && method != "CRÉDITO")
        if (id == 0) db.gastoDao().insert(Gasto(descripcion = description.trim(), monto = rules.amount(value), fecha = rules.now(), categoria = category, metodo = method, cajaId = db.operacionesDao().cajaAbierta()?.id))
        else {
            val old = requireNotNull(db.gastoDao().getAll().first().find { it.id == id }) { "El gasto ya no existe" }
            require(old.monto >= 0) { "Este registro es un abono antiguo; requiere conciliación" }
            require(old.cajaId == null || old.cajaId == db.operacionesDao().cajaAbierta()?.id) { "El gasto pertenece a una caja cerrada" }
            db.gastoDao().update(old.copy(descripcion = description.trim(), monto = rules.amount(value), categoria = category, metodo = method))
        }
    }
    suspend fun deleteExpense(g: Gasto) = db.withTransaction {
        require(g.monto >= 0) { "Este registro es un abono antiguo; requiere conciliación" }
        require(g.cajaId == null || g.cajaId == db.operacionesDao().cajaAbierta()?.id) { "El gasto pertenece a una caja cerrada" }
        db.gastoDao().delete(g)
    }
    suspend fun openBox(fund: Double) = db.withTransaction {
        positive(fund, "Fondo", true)
        require(db.operacionesDao().cajaAbierta() == null) { "Ya hay una caja abierta" }
        db.operacionesDao().insertCaja(Caja(apertura = rules.now(), fondo = rules.amount(fund)))
    }
    suspend fun closeBox(counted: Double) = db.withTransaction {
        positive(counted, "Efectivo contado", true)
        val c = requireNotNull(db.operacionesDao().cajaAbierta()) { "No hay caja abierta" }
        val expected = expectedCash(c, db.documentoDao().getAllWithItems().first(), db.operacionesDao().abonos().first(), db.gastoDao().getAll().first(), db.operacionesDao().reembolsos().first())
        db.operacionesDao().updateCaja(c.copy(cierre = rules.now(), contado = rules.amount(counted), esperado = expected))
    }
    suspend fun company(e: Empresa) {
        require(e.nombre.isNotBlank()) { "Escribe el nombre del negocio" }
        require(e.impuestoPorcentaje.isFinite() && e.impuestoPorcentaje in 0.0..100.0) { "Impuesto inválido" }
        require(e.formatoPapel in listOf("A4", "58", "80"))
        db.empresaDao().insert(e)
    }
    companion object {
        val METHODS = listOf("EFECTIVO", "TARJETA", "TRANSFERENCIA", "CRÉDITO")
        const val UNIT_PIECE = "PZA"
        const val UNIT_KILOGRAM = "KG"
        const val UNIT_LITER = "L"
        val UNITS = setOf(UNIT_PIECE, UNIT_KILOGRAM, UNIT_LITER)
        fun expectedCash(c: Caja, docs: List<DocumentoConItems>, payments: List<Abono>, expenses: List<Gasto>, refunds: List<Reembolso> = emptyList()): Double = BusinessRules.amount(c.fondo +
            docs.filter { it.documento.cajaId == c.id && it.documento.metodoPago == "EFECTIVO" }.sumOf { it.documento.total } +
            payments.filter { it.cajaId == c.id && it.metodo == "EFECTIVO" }.sumOf { it.monto } -
            expenses.filter { it.cajaId == c.id && it.metodo == "EFECTIVO" && it.monto > 0 }.sumOf { it.monto } -
            refunds.filter { it.cajaId == c.id && it.metodo == "EFECTIVO" }.sumOf { it.monto })
    }
}
