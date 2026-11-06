package com.tiketefactura.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiketefactura.data.BusinessRepository
import com.tiketefactura.data.local.*
import com.tiketefactura.util.*
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDate

@Composable
internal fun ExpensesScreen(vm: MainViewModel, expenses: List<Gasto>) {
    var add by remember { mutableStateOf(false) }; var deleting by remember { mutableStateOf<Gasto?>(null) }
    var editing by remember { mutableStateOf<Gasto?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = { add = true }) { Text("Registrar gasto") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (expenses.isEmpty()) item { Empty("Aún no hay gastos registrados") }
            items(expenses, key = { it.id }) { g -> CardBlock(g.descripcion) {
                Text("${money(g.monto)} · ${g.fecha} · ${g.categoria} · ${g.metodo}")
                if (g.monto < 0) Text("Abono antiguo: excluido de los gastos y del resultado estimado") else Row {
                    TextButton(onClick = { editing = g }) { Text("Editar") }; TextButton(onClick = { deleting = g }) { Text("Eliminar") }
                }
            } }
        }
    }
    if (add) ExpenseEditor(vm, null) { add = false }
    editing?.let { ExpenseEditor(vm, it) { editing = null } }
    deleting?.let { g -> Confirm("Eliminar gasto", "¿Eliminar ${g.descripcion} por ${money(g.monto)}?", { deleting = null }) { vm.action(after = { deleting = null }) { vm.repository.deleteExpense(g) } } }
}
@Composable
private fun ExpenseEditor(vm: MainViewModel, existing: Gasto?, dismiss: () -> Unit) {
    var description by remember { mutableStateOf(existing?.descripcion.orEmpty()) }; var value by remember { mutableStateOf(existing?.monto?.let(::initial).orEmpty()) }; var category by remember { mutableStateOf(existing?.categoria ?: "Mercancía") }; var method by remember { mutableStateOf(existing?.metodo ?: "EFECTIVO") }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (existing == null) "Registrar gasto" else "Editar gasto") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Input("Descripción", description, { description = it }); Input("Monto", value, { value = it }, true)
        Choices(listOf("Mercancía", "Servicios", "Renta", "Otros"), category) { category = it }; Choices(methods.filter { it != "CRÉDITO" }, method) { method = it }
    } }, confirmButton = { TextButton(onClick = { try { val amount = number(value); vm.action(after = dismiss) { vm.repository.expense(description, amount, category, method, existing?.id ?: 0) } } catch (e: Exception) { vm.error = e.message } }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
}
@Composable
internal fun CashScreen(vm: MainViewModel, boxes: List<Caja>, docs: List<DocumentoConItems>, payments: List<Abono>, expenses: List<Gasto>, refunds: List<Reembolso>) {
    var open by remember { mutableStateOf(false) }; var close by remember { mutableStateOf(false) }
    val active = boxes.find { it.cierre == null }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { CardBlock(if (active == null) "Caja cerrada" else "Caja #${active.id} abierta") {
            if (active == null) Button(onClick = { open = true }) { Text("Abrir caja") } else {
                Text("Apertura: ${active.apertura} · Fondo: ${money(active.fondo)}")
                methods.filter { it != "CRÉDITO" }.forEach { m -> val collected = docs.filter { it.documento.cajaId == active.id && it.documento.metodoPago == m }.sumOf { it.documento.total } + payments.filter { it.cajaId == active.id && it.metodo == m }.sumOf { it.monto } - refunds.filter { it.cajaId == active.id && it.metodo == m }.sumOf { it.monto }; Text("Cobros $m: ${money(collected)}") }
                Text("Efectivo esperado: ${money(BusinessRepository.expectedCash(active, docs, payments, expenses, refunds))}", fontWeight = FontWeight.Bold)
                Button(onClick = { close = true }) { Text("Cerrar y contar caja") }
            }
            Text("Abre la caja antes de cobrar. Las operaciones realizadas sin caja abierta quedan fuera de sus cierres.", style = MaterialTheme.typography.bodySmall)
        } }
        items(boxes.filter { it.cierre != null }, key = { it.id }) { c -> CardBlock("Cierre #${c.id}") {
            Text("${c.apertura} → ${c.cierre}"); Text("Esperado: ${money(c.esperado ?: 0.0)} · Contado: ${money(c.contado ?: 0.0)}")
            Text("Diferencia: ${money((c.contado ?: 0.0) - (c.esperado ?: 0.0))}", fontWeight = FontWeight.Bold)
        } }
        if (refunds.isNotEmpty()) item { Text("Reembolsos", style = MaterialTheme.typography.titleLarge) }
        items(refunds, key = { "refund-${it.id}" }) { r -> CardBlock("Reembolso #${r.id}") {
            Text("Venta #${r.documentoId} · ${money(r.monto)} · ${r.metodo}")
            Text("${r.fecha} · Caja ${r.cajaId ?: "sin caja"}"); Text(r.motivo)
        } }
    }
    if (open) FormDialog("Abrir caja", listOf(Field("f", "Fondo inicial de efectivo", "0", true)), { open = false }) { v, dismiss -> val f = number(v.getValue("f")); vm.action(after = dismiss) { vm.repository.openBox(f) } }
    if (close) FormDialog("Cerrar caja", listOf(Field("c", "Efectivo contado", "", true)), { close = false }) { v, dismiss -> val c = number(v.getValue("c")); vm.action(after = dismiss) { vm.repository.closeBox(c) } }
}

@Composable
internal fun SettingsScreen(vm: MainViewModel, company: Empresa?, docs: List<DocumentoConItems>) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }; var ruc by rememberSaveable { mutableStateOf("") }; var address by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }; var slogan by rememberSaveable { mutableStateOf("") }; var footer by rememberSaveable { mutableStateOf("") }
    var tax by rememberSaveable { mutableStateOf(true) }; var rate by rememberSaveable { mutableStateOf("15") }; var noStock by rememberSaveable { mutableStateOf(false) }
    var paper by rememberSaveable { mutableStateOf("A4") }; var logo by remember { mutableStateOf<String?>(null) }; var restore by remember { mutableStateOf<String?>(null) }
    var printer by remember { mutableStateOf(false) }
    LaunchedEffect(company) { company?.let { name = it.nombre; ruc = it.ruc.orEmpty(); address = it.direccion.orEmpty(); phone = it.telefono.orEmpty(); slogan = it.eslogan.orEmpty(); footer = it.mensajePie.orEmpty(); tax = it.impuestoActivo; rate = initial(it.impuestoPorcentaje); noStock = it.permitirSinStock; paper = it.formatoPapel; logo = it.logoUri } }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) {
        var encoded: String? = null
        vm.action("Logo cargado; guarda los ajustes", after = { logo = encoded }) {
            val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)!!.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
            val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = (maxOf(bounds.outWidth, bounds.outHeight) / 800).coerceAtLeast(1) }
            val bitmap = context.contentResolver.openInputStream(uri)!!.use { android.graphics.BitmapFactory.decodeStream(it, null, options) } ?: error("No se pudo leer la imagen")
            encoded = PdfGenerator.encode(bitmap); bitmap.recycle()
        }
    } }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) vm.action("Respaldo guardado") { val json = BackupManager.export(vm.db); context.contentResolver.openOutputStream(uri, "wt")!!.bufferedWriter().use { it.write(json) } } }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) {
        var text = ""; vm.action("Respaldo leído; confirma la restauración", after = { restore = text }) { text = BackupManager.read(context, uri) }
    } }
    val recoveryExport = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) vm.action("Copia de recuperación guardada") {
        val file = File(context.filesDir, "respaldo-antes-de-restaurar.json"); require(file.exists()) { "Todavía no hay copia de recuperación" }
        context.contentResolver.openOutputStream(uri, "wt")!!.use { output -> file.inputStream().use { it.copyTo(output) } }
    } }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { CardBlock("Negocio") {
            LogoPreview(logo, Modifier.size(80.dp))
            Input("Nombre del negocio", name, { name = it }); Input("Identificación fiscal", ruc, { ruc = it }); Input("Dirección", address, { address = it })
            Input("Teléfono", phone, { phone = it }); Input("Eslogan", slogan, { slogan = it }); Input("Mensaje al pie", footer, { footer = it })
            TextButton(onClick = { logoPicker.launch("image/*") }) { Text(if (logo == null) "Agregar logo" else "Cambiar logo cargado") }; if (logo != null) TextButton(onClick = { logo = null }) { Text("Quitar logo") }
        } }
        item { CardBlock("Ventas y comprobantes") {
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Impuesto activo", Modifier.weight(1f)); Switch(tax, { tax = it }) }; Input("Porcentaje de impuesto", rate, { rate = it }, true)
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Permitir ventas sin stock", Modifier.weight(1f)); Switch(noStock, { noStock = it }) }
            Text("Papel: A4 o ticket en mm"); Choices(listOf("A4", "58", "80"), paper) { paper = it }
            OutlinedButton(onClick = { printer = true }) { Text("Configurar impresora Bluetooth") }
            Button(onClick = { try { val e = Empresa(nombre = name.trim(), ruc = ruc, direccion = address, telefono = phone, eslogan = slogan, mensajePie = footer, logoUri = logo, impuestoActivo = tax, impuestoPorcentaje = number(rate), permitirSinStock = noStock, formatoPapel = paper); vm.action { vm.repository.company(e) } } catch (e: Exception) { vm.error = e.message } }) { Text("Guardar ajustes") }
        } }
        item { CardBlock("Respaldo y exportación") {
            Text("El respaldo incluye clientes, ventas, inventario, caja, ajustes, logos y firmas guardados en esta versión.")
            Button(onClick = { export.launch("TiketeFactura-${LocalDate.now()}.json") }) { Text("Guardar respaldo completo") }
            OutlinedButton(onClick = { import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }) { Text("Restaurar respaldo") }
            TextButton(onClick = { recoveryExport.launch("TiketeFactura-recuperacion.json") }) { Text("Exportar copia anterior a la restauración") }
            TextButton(onClick = { vm.action("CSV preparado") { val f = File(context.cacheDir, "Ventas.csv"); f.writeText(Sharing.csv(docs)); withContext(Dispatchers.Main) { Sharing.file(context, f, "text/csv") } } }) { Text("Exportar ventas a CSV para Excel") }
        } }
        item { SubscriptionCard() }
    }
    restore?.let { text -> Confirm("Reemplazar datos con respaldo", "Se reemplazarán los datos locales. Primero se conservará una copia de recuperación dentro de la aplicación. Restaura un respaldo que hayas creado y reconocido.", { restore = null }) {
        vm.action("Respaldo restaurado", after = { restore = null; vm.clear(); vm.receipt = null; vm.paymentReceipt = null }) {
            val recovery = BackupManager.export(vm.db); File(context.filesDir, "respaldo-antes-de-restaurar.json").writeText(recovery); BackupManager.restore(vm.db, text)
        }
    } }
    if (printer) PrinterDialog(vm, null, company) { printer = false }
}
