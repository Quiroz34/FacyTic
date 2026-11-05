package com.tiketefactura.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiketefactura.data.local.*
import kotlinx.coroutines.flow.first

@Composable
internal fun ProductsScreen(vm: MainViewModel, products: List<Producto>, categories: List<Categoria>) {
    var query by rememberSaveable { mutableStateOf("") }; var category by rememberSaveable { mutableStateOf("Todas") }
    var editing by remember { mutableStateOf<Producto?>(null) }; var deleting by remember { mutableStateOf<Producto?>(null) }; var manage by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { editing = Producto(nombre = "", precio = 0.0) }) { Text("Agregar producto") }; OutlinedButton(onClick = { manage = true }) { Text("Categorías") } }
        Input("Buscar por nombre o código", query, { query = it }); Choices(listOf("Todas") + categories.map { it.nombre }, category) { category = it }
        val filtered = products.filter { (it.nombre.contains(query, true) || it.codigoBarras.orEmpty().contains(query, true)) && (category == "Todas" || categories.find { c -> c.id == it.categoriaId }?.nombre == category) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (filtered.isEmpty()) item { Empty("No hay productos. Agrega uno para comenzar.") }
            items(filtered, key = { it.id }) { p -> CardBlock(p.nombre) {
                Text("${money(p.precio)} por ${unitName(p.unidad)} · Stock ${formatQuantity(p.stock, p.unidad)} · Costo ${money(p.costo)}")
                if (!p.codigoBarras.isNullOrBlank()) Text("Código: ${p.codigoBarras}"); if (p.proveedor.isNotBlank()) Text("Proveedor: ${p.proveedor}")
                Row { TextButton(onClick = { editing = p }) { Text("Editar") }; TextButton(onClick = { deleting = p }) { Text("Eliminar") } }
            } }
        }
    }
    editing?.let { p -> ProductEditor(vm, p, categories) { editing = null } }
    deleting?.let { p -> Confirm("Eliminar producto", "¿Eliminar ${p.nombre}? Los productos con ventas se conservan para proteger el historial.", { deleting = null }) { vm.action(after = { deleting = null }) { vm.repository.deleteProduct(p) } } }
    if (manage) CategoryDialog(vm, categories) { manage = false }
}
@Composable
private fun ProductEditor(vm: MainViewModel, p: Producto, categories: List<Categoria>, dismiss: () -> Unit) {
    var name by remember { mutableStateOf(p.nombre) }; var price by remember { mutableStateOf(initial(p.precio)) }
    var cost by remember { mutableStateOf(initial(p.costo)) }; var stock by remember { mutableStateOf(initial(p.stock)) }
    var minimum by remember { mutableStateOf(initial(p.stockMinimo)) }; var code by remember { mutableStateOf(p.codigoBarras.orEmpty()) }
    var supplier by remember { mutableStateOf(p.proveedor) }; var unit by remember { mutableStateOf(p.unidad) }; var cat by remember { mutableStateOf(p.categoriaId) }; var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = dismiss, title = { Text(if (p.id == 0) "Nuevo producto" else "Editar producto") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Input("Nombre", name, { name = it }); Input("Precio de venta", price, { price = it }, true); Input("Costo de compra", cost, { cost = it }, true)
            if (p.id == 0) Input("Stock inicial", stock, { stock = it }, true) else Text("Stock actual: ${p.stock}. Modifícalo en Inventario.")
            Choices(listOf("PZA", "KG", "L"), unit) { unit = it }
            Text("Unidad: ${unitName(unit)}. Kilogramos y litros aceptan decimales, por ejemplo 0.5.", style = MaterialTheme.typography.bodySmall)
            Input("Stock mínimo", minimum, { minimum = it }, true); Input("Código de barras", code, { code = it }); BarcodePhoto(vm) { code = it }
            Input("Proveedor", supplier, { supplier = it })
            Choices(listOf("Sin categoría") + categories.map { it.nombre }, categories.find { it.id == cat }?.nombre ?: "Sin categoría") { selected -> cat = categories.find { it.nombre == selected }?.id }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onClick = { try {
        val value = p.copy(nombre = name.trim(), precio = number(price), costo = number(cost), stock = number(stock), stockMinimo = number(minimum), codigoBarras = code.trim().ifBlank { null }, proveedor = supplier.trim(), categoriaId = cat, unidad = unit)
        vm.action(after = dismiss) { vm.repository.product(value) }
    } catch (e: Exception) { error = e.message } }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
}
@Composable
private fun CategoryDialog(vm: MainViewModel, categories: List<Categoria>, dismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }; var selected by remember { mutableStateOf<Categoria?>(null) }; var deleting by remember { mutableStateOf<Categoria?>(null) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Categorías") }, text = { Column {
        Input("Nombre", name, { name = it })
        Button(onClick = { val value = name.trim(); val current = selected; vm.action(after = { name = ""; selected = null }) {
            vm.repository.category((current ?: Categoria(nombre = value)).copy(nombre = value))
        } }) { Text(if (selected == null) "Agregar" else "Guardar cambios") }
        LazyColumn(Modifier.heightIn(max = 320.dp)) { items(categories) { c -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(c.nombre, Modifier.weight(1f)); IconButton(onClick = { selected = c; name = c.nombre }) { Icon(Icons.Default.Edit, "Editar") }; IconButton(onClick = { deleting = c }) { Icon(Icons.Default.Delete, "Eliminar") }
        } } }
    } }, confirmButton = { TextButton(onClick = dismiss) { Text("Cerrar") } })
    deleting?.let { c -> Confirm("Eliminar categoría", "Los productos de ${c.nombre} quedarán sin categoría.", { deleting = null }) { vm.action(after = { deleting = null }) { vm.db.categoriaDao().delete(c) } } }
}

@Composable
internal fun InventoryScreen(vm: MainViewModel, products: List<Producto>, movements: List<InventarioMovimiento>) {
    var query by rememberSaveable { mutableStateOf("") }; var product by remember { mutableStateOf<Producto?>(null) }; var filter by rememberSaveable { mutableStateOf("Todos") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Input("Buscar producto", query, { query = it }); Choices(listOf("Todos", "Stock bajo", "Movimientos"), filter) { filter = it }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (filter != "Movimientos") {
                items(products.filter { it.nombre.contains(query, true) && (filter != "Stock bajo" || it.stock <= it.stockMinimo) }) { p -> CardBlock(p.nombre) {
                    Text("Stock: ${formatQuantity(p.stock, p.unidad)} · Mínimo: ${formatQuantity(p.stockMinimo, p.unidad)}"); Text("Costo: ${money(p.costo)} por ${unitName(p.unidad)} · Proveedor: ${p.proveedor.ifBlank { "Sin asignar" }}")
                    TextButton(onClick = { product = p }) { Text("Entrada / ajuste") }
                } }
            } else {
                if (movements.isEmpty()) item { Empty("Sin movimientos de inventario") }
                items(movements.filter { it.productoNombre.contains(query, true) }, key = { it.id }) { m -> CardBlock(m.productoNombre) { Text("${m.cantidad} · ${m.tipo}"); Text(m.fecha) } }
            }
        }
    }
    product?.let { p -> FormDialog("Movimiento: ${p.nombre}", listOf(Field("q", "${unitName(p.unidad)}: positiva entra, negativa sale${if (p.unidad == "PZA") " (entero)" else " (admite decimales)"}", "", true), Field("r", "Motivo / referencia de compra")), { product = null }) { v, close ->
        val qty = number(v.getValue("q")); vm.action(after = close) { vm.repository.adjust(p.id, qty, v.getValue("r")) }
    } }
}
