package com.tiketefactura.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiketefactura.data.BusinessRules
import com.tiketefactura.data.local.*
import com.tiketefactura.util.*
import kotlinx.coroutines.*
import java.io.File

@Composable
internal fun ClientsScreen(vm: MainViewModel, clients: List<Cliente>, docs: List<DocumentoConItems>, payments: List<Abono>) {
    var query by rememberSaveable { mutableStateOf("") }; var editing by remember { mutableStateOf<Cliente?>(null) }
    var selected by remember { mutableStateOf<Cliente?>(null) }; var deleting by remember { mutableStateOf<Cliente?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { editing = Cliente(nombre = "") }) { Text("Nuevo cliente") }; Input("Buscar cliente o teléfono", query, { query = it })
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (clients.isEmpty()) item { Empty("Registra clientes para llevar sus créditos y abonos") }
            items(clients.filter { it.nombre.contains(query, true) || it.telefono.orEmpty().contains(query) }, key = { it.id }) { c -> CardBlock(c.nombre) {
                Text("Saldo: ${money(c.deuda)}", fontWeight = FontWeight.Bold); c.telefono?.let { Text(it) }
                Row { TextButton(onClick = { selected = c }) { Text("Estado de cuenta") }; TextButton(onClick = { editing = c }) { Text("Editar") }; TextButton(onClick = { deleting = c }) { Text("Eliminar") } }
            } }
        }
    }
    editing?.let { c -> FormDialog(if (c.id == 0) "Nuevo cliente" else "Editar cliente", listOf(Field("n", "Nombre", c.nombre), Field("t", "Teléfono", c.telefono.orEmpty()), Field("e", "Correo", c.correo.orEmpty()), Field("d", "Dirección", c.direccion.orEmpty())), { editing = null }) { v, close ->
        vm.action(after = close) { vm.repository.client(c.copy(nombre = v.getValue("n").trim(), telefono = v["t"], correo = v["e"], direccion = v["d"])) }
    } }
    selected?.let { original -> val fresh = clients.find { it.id == original.id } ?: original; AccountDialog(vm, fresh, docs.filter { it.documento.clienteId == fresh.id }, payments.filter { it.clienteId == fresh.id }) { selected = null } }
    deleting?.let { c -> Confirm("Eliminar cliente", "¿Eliminar ${c.nombre}? Solo se permite si no tiene deuda ni historial.", { deleting = null }) { vm.action(after = { deleting = null }) { vm.repository.deleteClient(c) } } }
}
@Composable
private fun AccountDialog(vm: MainViewModel, client: Cliente, docs: List<DocumentoConItems>, payments: List<Abono>, dismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }; var method by remember { mutableStateOf("EFECTIVO") }; var document by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    AlertDialog(onDismissRequest = dismiss, title = { Text(client.nombre) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Saldo actual: ${money(client.deuda)}", fontWeight = FontWeight.Bold); Text("Aplicar abono a:")
        Choices(listOf("Más antiguas") + docs.filter { it.documento.estado == "PENDIENTE" }.map { it.documento.numero }, docs.find { it.documento.id == document }?.documento?.numero ?: "Más antiguas") { label -> document = docs.find { it.documento.numero == label }?.documento?.id }
        Input("Monto del abono", amount, { amount = it }, true); Choices(methods.filter { it != "CRÉDITO" }, method) { method = it }
        Button(enabled = !vm.busy && client.deuda > 0, onClick = { try { val value = number(amount); var saved = emptyList<Abono>(); vm.action("Abono registrado", after = { amount = ""; vm.paymentReceipt = client to saved }) { saved = vm.repository.payment(client.id, value, method, document) } } catch (e: Exception) { vm.error = e.message } }) { Text("Registrar abono") }
        Text("Comprobantes", fontWeight = FontWeight.Bold)
        docs.forEach { d -> TextButton(onClick = { vm.receipt = d }) { Text("${d.documento.numero} · ${d.documento.estado} · saldo ${money(if (d.documento.estado == "ANULADA") 0.0 else d.documento.total - d.documento.abonado)}") } }
        Text("Abonos", fontWeight = FontWeight.Bold); if (payments.isEmpty()) Text("Sin abonos registrados en esta versión")
        payments.forEach { a -> TextButton(onClick = { runCatching { Sharing.file(context, PdfGenerator.receipt(context, client, listOf(a)), "application/pdf") }.onFailure { vm.error = it.message } }) { Text("#${a.id} · ${a.fecha} · ${money(a.monto)} · ${a.metodo}") } }
        Text("Los saldos migrados pueden incluir abonos antiguos sin recibo ni asignación a una venta.", style = MaterialTheme.typography.bodySmall)
    } }, confirmButton = { TextButton(onClick = dismiss) { Text("Cerrar") } })
}

@Composable
internal fun HistoryScreen(vm: MainViewModel, docs: List<DocumentoConItems>, company: Empresa?) {
    var query by rememberSaveable { mutableStateOf("") }; var start by rememberSaveable { mutableStateOf("") }; var end by rememberSaveable { mutableStateOf("") }
    var method by rememberSaveable { mutableStateOf("Todos") }; var state by rememberSaveable { mutableStateOf("Todos") }
    var selected by remember { mutableStateOf<DocumentoConItems?>(null) }; var cancel by remember { mutableStateOf<DocumentoConItems?>(null) }
    val context = LocalContext.current
    val from = BusinessRules.datePart(start); val until = BusinessRules.datePart(end)
    val datePattern = Regex("\\d{4}-\\d{2}-\\d{2}")
    val validDates = (start.isBlank() || datePattern.matches(start) && from != null) && (end.isBlank() || datePattern.matches(end) && until != null) && (from == null || until == null || !from.isAfter(until))
    val filtered = docs.filter { full -> val d = full.documento; val day = BusinessRules.datePart(d.fecha)
        validDates && (d.numero.contains(query, true) || d.clienteNombre.orEmpty().contains(query, true)) && (method == "Todos" || d.metodoPago == method) && (state == "Todos" || d.estado == state) &&
            (from == null || day != null && !day.isBefore(from)) && (until == null || day != null && !day.isAfter(until)) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Input("Buscar folio o cliente", query, { query = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Input("Desde AAAA-MM-DD", start, { start = it }, modifier = Modifier.weight(1f)); Input("Hasta AAAA-MM-DD", end, { end = it }, modifier = Modifier.weight(1f)) }
        if (!validDates) Text("Revisa el formato o el orden de las fechas", color = MaterialTheme.colorScheme.error)
        Choices(listOf("Todos") + methods, method) { method = it }; Choices(listOf("Todos", "PAGADA", "PENDIENTE", "ANULADA"), state) { state = it }
        TextButton(onClick = { vm.action("CSV preparado") { val f = File(context.cacheDir, "Ventas.csv"); f.writeText(Sharing.csv(filtered)); withContext(Dispatchers.Main) { Sharing.file(context, f, "text/csv") } } }) { Text("Exportar resultados a CSV") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (filtered.isEmpty()) item { Empty("No hay comprobantes con estos filtros") }
            items(filtered, key = { it.documento.id }) { full -> val d = full.documento; CardBlock("${d.numero} · ${money(d.total)}") {
                Text("${d.clienteNombre} · ${d.fecha}"); Text("${d.metodoPago} · ${d.estado}")
                Row { TextButton(onClick = { selected = full }) { Text("Ver comprobante") }; if (d.estado != "ANULADA") TextButton(onClick = { cancel = full }) { Text("Cancelar / devolver") } }
            } }
        }
    }
    selected?.let { val latest = docs.find { d -> d.documento.id == it.documento.id } ?: it; DocumentDialog(latest, company, { selected = null }, vm) }
    cancel?.let { full -> FormDialog("Cancelar y devolver venta completa", listOf(Field("r", "Motivo y confirmación del reembolso")), { cancel = null }) { values, close ->
        vm.action("Venta anulada e inventario restituido", after = close) { vm.repository.cancel(full.documento.id, values.getValue("r")) }
    } }
}
@Composable
internal fun DocumentDialog(full: DocumentoConItems, company: Empresa?, dismiss: () -> Unit, vm: MainViewModel) {
    val context = LocalContext.current; val d = full.documento
    var bluetooth by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = dismiss, title = { Text(d.numero) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("${d.clienteNombre} · ${d.fecha}"); Text("${d.estado} · ${d.metodoPago}"); full.items.forEach { Text("${it.cantidad} × ${it.nombre}: ${money(it.subtotal)}") }
        Text("Subtotal: ${money(d.subtotal)}\nDescuento: ${money(d.descuento)}\nImpuesto: ${money(d.impuesto)}\nTotal: ${money(d.total)}")
        if (d.metodoPago == "CRÉDITO") Text("Abonado: ${money(d.abonado)} · Saldo: ${money(if (d.estado == "ANULADA") 0.0 else d.total - d.abonado)}")
        d.motivoAnulacion?.let { Text("Motivo: $it") }; if (!d.firma.isNullOrBlank()) Text("Incluye firma del cliente")
        Button(onClick = { runCatching { PdfGenerator.generateAndPrint(context, full, company) }.onFailure { vm.error = it.message } }) { Text("Imprimir / guardar PDF") }
        Button(onClick = { bluetooth = true }) { Text("Imprimir ticket Bluetooth") }
        OutlinedButton(onClick = { vm.action("PDF preparado") { val file = PdfGenerator.create(context, full, company); withContext(Dispatchers.Main) { Sharing.file(context, file, "application/pdf") } } }) { Text("Compartir PDF") }
    } }, confirmButton = { TextButton(onClick = dismiss) { Text("Cerrar") } })
    if (bluetooth) PrinterDialog(vm, full, company) { bluetooth = false }
}
