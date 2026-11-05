package com.tiketefactura.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.tiketefactura.data.BusinessRules
import com.tiketefactura.data.local.*
import com.tiketefactura.util.PdfGenerator
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File

@Composable
internal fun BarcodePhoto(vm: MainViewModel, found: (String) -> Unit) {
    val context = LocalContext.current
    val scanner = remember { BarcodeScanning.getClient() }
    DisposableEffect(scanner) { onDispose { scanner.close() } }
    val filename = rememberSaveable { "barcode-${java.util.UUID.randomUUID()}.jpg" }
    val photo = remember(filename) { File(context.cacheDir, filename) }
    val uri = remember(photo) { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photo) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        if (taken) runCatching { scanner.process(InputImage.fromFilePath(context, uri)).addOnSuccessListener { codes ->
            val value = codes.firstOrNull()?.rawValue
            if (value == null) vm.error = "No se detectó el código; acerca la cámara o escríbelo" else found(value)
        }.addOnFailureListener { vm.error = "No se pudo leer el código: ${it.message}" }
            .addOnCompleteListener { photo.delete() }
        }.onFailure { vm.error = "No se pudo leer la foto"; photo.delete() }
    }
    TextButton(onClick = { runCatching { launcher.launch(uri) }.onFailure { vm.error = "No hay cámara disponible; escribe el código" } }) { Text("Leer código con cámara") }
}

@Composable
internal fun SaleScreen(vm: MainViewModel, products: List<Producto>, categories: List<Categoria>, clients: List<Cliente>, company: Empresa?) {
    var name by rememberSaveable { mutableStateOf("") }; var clientId by rememberSaveable { mutableStateOf<Int?>(null) }
    var method by rememberSaveable { mutableStateOf("EFECTIVO") }; var type by rememberSaveable { mutableStateOf("TICKET") }
    var discount by rememberSaveable { mutableStateOf("0") }; var cash by rememberSaveable { mutableStateOf("") }
    var tax by rememberSaveable(company?.impuestoActivo) { mutableStateOf(company?.impuestoActivo ?: true) }
    var query by rememberSaveable { mutableStateOf("") }; var category by rememberSaveable { mutableStateOf("Todas") }
    var picker by remember { mutableStateOf(false) }; var signature by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf<CartItem?>(null) }; var clear by remember { mutableStateOf(false) }
    val cart = vm.cart
    val totalsResult = runCatching { BusinessRules.totals(cart.map { it.producto.precio to it.cantidad }, number(discount), if (tax) company?.impuestoPorcentaje ?: 15.0 else 0.0) }
    val totals = totalsResult.getOrNull()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
        item { CardBlock("Cliente y pago") {
            Input("Cliente", name, { name = it; clientId = null }); TextButton(onClick = { picker = true }) { Text("Seleccionar cliente registrado") }
            Choices(listOf("TICKET", "FACTURA"), type) { type = it }; Choices(methods, method) { method = it }
            if (method == "CRÉDITO" && clientId == null) Text("Selecciona un cliente registrado para crédito", color = MaterialTheme.colorScheme.error)
        } }
        item { CardBlock("Agregar productos") {
            Input("Buscar nombre o código de barras", query, { query = it })
            BarcodePhoto(vm) { code -> val p = products.find { it.codigoBarras == code }; if (p == null) vm.message = "Código no registrado: $code" else { vm.add(p); query = "" } }
            Choices(listOf("Todas") + categories.map { it.nombre }, category) { category = it }
            val filtered = products.filter { (it.nombre.contains(query, true) || it.codigoBarras.orEmpty().contains(query, true)) && (category == "Todas" || categories.find { c -> c.id == it.categoriaId }?.nombre == category) }.take(20)
            if (filtered.isEmpty()) Text("No se encontraron productos")
            filtered.forEach { p -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(p.nombre); Text("${money(p.precio)} por ${unitName(p.unidad)} · disponibles ${formatQuantity(p.stock, p.unidad)}", style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = { vm.add(p) }, enabled = !vm.busy) { Icon(Icons.Default.AddCircle, "Agregar ${p.nombre}") }
            } }
            if (products.size > 20 && query.isBlank()) Text("Busca para encontrar más productos", style = MaterialTheme.typography.bodySmall)
        } }
        item { Text("Carrito", style = MaterialTheme.typography.titleLarge) }
        if (cart.isEmpty()) item { Empty("Agrega un producto para empezar la venta") }
        items(cart, key = { it.producto.id }) { item -> CardBlock(item.producto.nombre) {
            Text("${formatQuantity(item.cantidad, item.producto.unidad)} × ${money(item.producto.precio)} = ${money(BusinessRules.lineTotal(item.producto.precio, item.cantidad))}")
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.quantity(item.producto, (item.cantidad - if (item.producto.unidad == "PZA") 1.0 else 0.1).coerceAtLeast(0.0)) }) { Icon(Icons.Default.Remove, "Restar") }
                TextButton(onClick = { quantity = item }) { Text("Cantidad: ${formatQuantity(item.cantidad, item.producto.unidad)}") }
                IconButton(onClick = { vm.add(item.producto) }) { Icon(Icons.Default.Add, "Sumar") }
                IconButton(onClick = { vm.quantity(item.producto, 0.0) }) { Icon(Icons.Default.Delete, "Quitar") }
            }
        } }
        item { CardBlock("Totales") {
            Input("Descuento en importe", discount, { discount = it }, true)
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Impuesto ${company?.impuestoPorcentaje ?: 15.0}%", Modifier.weight(1f)); Switch(tax, { tax = it }) }
            totals?.let { Text("Subtotal: ${money(it.subtotal)}"); Text("Descuento: ${money(it.discount)}"); Text("Impuesto: ${money(it.tax)}"); Text("Total: ${money(it.total)}", style = MaterialTheme.typography.headlineSmall) }
            if (cart.isNotEmpty() && totals == null) Text(totalsResult.exceptionOrNull()?.message ?: "Revisa los importes", color = MaterialTheme.colorScheme.error)
            if (method == "EFECTIVO") { Input("Efectivo recibido", cash, { cash = it }, true); val change = runCatching { number(cash) - (totals?.total ?: 0.0) }.getOrDefault(0.0); Text("Cambio: ${money(change.coerceAtLeast(0.0))}") }
            Button(enabled = !vm.busy && totals != null && (method != "CRÉDITO" || clientId != null), modifier = Modifier.fillMaxWidth(), onClick = {
                try { if (method == "EFECTIVO") require(number(cash) >= totals!!.total) { "El efectivo recibido no cubre el total" }; signature = true } catch (e: Exception) { vm.error = e.message }
            }) { Text("Confirmar venta") }
            TextButton(onClick = { clear = true }, enabled = cart.isNotEmpty()) { Text("Vaciar carrito") }
        } }
    }
    if (picker) SearchClientDialog(clients, { picker = false }) { c -> name = c.nombre; clientId = c.id; picker = false }
    quantity?.let { item -> FormDialog("Cantidad de ${item.producto.nombre}", listOf(Field("q", if (item.producto.unidad == "PZA") "Piezas (número entero)" else "${unitName(item.producto.unidad)} (admite decimales, por ejemplo 0.5)", initial(item.cantidad), true)), { quantity = null }) { values, close -> vm.quantity(item.producto, number(values.getValue("q"))); close() } }
    if (clear) Confirm("Vaciar carrito", "Se quitarán los productos de esta venta.", { clear = false }) { vm.clear(); clear = false }
    if (signature) SignatureDialog({ signature = false }) { signed ->
        vm.sale(name, clientId, type, method, number(discount), tax, if (method == "EFECTIVO") number(cash) else 0.0, signed) { name = ""; clientId = null; discount = "0"; cash = ""; signature = false }
    }
}
@Composable
private fun SearchClientDialog(clients: List<Cliente>, dismiss: () -> Unit, select: (Cliente) -> Unit) {
    var query by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Seleccionar cliente") }, text = { Column {
        Input("Buscar cliente o teléfono", query, { query = it })
        LazyColumn(Modifier.heightIn(max = 380.dp)) { items(clients.filter { it.nombre.contains(query, true) || it.telefono.orEmpty().contains(query) }) { c -> TextButton(onClick = { select(c) }) { Text("${c.nombre} · saldo ${money(c.deuda)}") } } }
    } }, confirmButton = { TextButton(onClick = dismiss) { Text("Cerrar") } })
}
@Composable
private fun SignatureDialog(dismiss: () -> Unit, confirm: (String?) -> Unit) {
    var strokes by remember { mutableStateOf(listOf<List<androidx.compose.ui.geometry.Offset>>()) }
    var size by remember { mutableStateOf(androidx.compose.ui.geometry.Size(1f, 1f)) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Firma opcional") }, text = { Column {
        Text("Firma aquí o confirma sin firma")
        Canvas(Modifier.fillMaxWidth().height(180.dp).background(Color.White).border(1.dp, Color.Gray).pointerInput(Unit) {
            detectDragGestures(onDragStart = { point -> strokes = strokes + listOf(listOf(point)) }, onDrag = { change, _ -> change.consume(); strokes = strokes.dropLast(1) + listOf((strokes.lastOrNull() ?: emptyList()) + change.position) })
        }) { size = this.size; strokes.forEach { points -> val path = Path(); points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }; drawPath(path, Color.Black, style = Stroke(width = 4f)) } }
        TextButton(onClick = { strokes = emptyList() }) { Text("Limpiar") }
    } }, confirmButton = { TextButton(onClick = {
        val data = if (strokes.isEmpty()) null else {
            val bitmap = Bitmap.createBitmap(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            val canvas = AndroidCanvas(bitmap); canvas.drawColor(AndroidColor.WHITE)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.BLACK; style = Paint.Style.STROKE; strokeWidth = 4f; strokeCap = Paint.Cap.ROUND }
            strokes.forEach { points -> val path = AndroidPath(); points.forEachIndexed { i, p -> if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y) }; canvas.drawPath(path, paint) }
            PdfGenerator.encode(bitmap).also { bitmap.recycle() }
        }; confirm(data)
    }) { Text("Guardar venta") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Volver") } })
}
