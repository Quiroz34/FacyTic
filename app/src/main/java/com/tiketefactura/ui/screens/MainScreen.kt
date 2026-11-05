package com.tiketefactura.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tiketefactura.data.*
import com.tiketefactura.data.local.*
import com.tiketefactura.util.*
import java.time.LocalDate
import com.tiketefactura.R
import kotlinx.coroutines.launch

internal fun money(v: Double) = PdfGenerator.money(v)
internal fun number(value: String): Double = value.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() } ?: error("Escribe un número válido")
internal fun initial(v: Double) = if (v == 0.0) "0" else v.toString()
internal fun unitName(unit: String) = when (unit) { "KG" -> "kg"; "L" -> "L"; else -> "pieza" }
internal fun formatQuantity(value: Double, unit: String) = "${java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()} ${unitName(unit)}"
internal val methods = BusinessRepository.METHODS

private data class AppDestination(val title: String, val subtitle: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = hiltViewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val destinations = listOf(
        AppDestination("Inicio", "El pulso de tu negocio", Icons.Default.Home),
        AppDestination("Nueva venta", "Cobra rápido y sin errores", Icons.Default.PointOfSale),
        AppDestination("Productos", "Catálogo, precios y categorías", Icons.Default.Inventory2),
        AppDestination("Clientes", "Contactos, crédito y abonos", Icons.Default.People),
        AppDestination("Historial", "Consulta y comparte comprobantes", Icons.AutoMirrored.Filled.ReceiptLong),
        AppDestination("Inventario", "Existencias y movimientos", Icons.Default.Warehouse),
        AppDestination("Gastos", "Registra las salidas del negocio", Icons.Default.Payments),
        AppDestination("Caja", "Aperturas, cobros y cierres", Icons.Default.AccountBalanceWallet),
        AppDestination("Ajustes", "Personaliza y protege tus datos", Icons.Default.Settings)
    )
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val products by vm.productos.collectAsState(); val clients by vm.clientes.collectAsState()
    val categories by vm.categorias.collectAsState(); val docs by vm.documentos.collectAsState()
    val company by vm.empresa.collectAsState(); val expenses by vm.gastos.collectAsState()
    val refunds by vm.reembolsos.collectAsState(); val movements by vm.movimientos.collectAsState(); val payments by vm.abonos.collectAsState(); val boxes by vm.cajas.collectAsState()
    LaunchedEffect(products, vm.busy) { vm.refreshCart(products) }
    LaunchedEffect(vm.message) { vm.message?.let { snackbar.showSnackbar(it); vm.message = null } }
    ModalNavigationDrawer(drawerState = drawer, drawerContent = {
        ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).statusBarsPadding().padding(24.dp)) {
                LogoPreview(company?.logoUri, Modifier.size(56.dp))
                Spacer(Modifier.height(16.dp))
                Text(company?.nombre ?: "Tikete y Factura", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("Todo tu negocio en un solo lugar", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .72f), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            destinations.forEachIndexed { i, destination -> NavigationDrawerItem(
                icon = { Icon(destination.icon, null) },
                label = { Text(destination.title) },
                selected = i == tab,
                onClick = { tab = i; scope.launch { drawer.close() } },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                shape = RoundedCornerShape(14.dp)
            ) }
        }
    }, gesturesEnabled = !vm.busy) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { TopAppBar(
                title = { Column {
                    Text(destinations[tab].title, style = MaterialTheme.typography.titleLarge)
                    Text(destinations[tab].subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } },
                navigationIcon = { IconButton(onClick = { scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, "Abrir menú") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            ) },
            bottomBar = { NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                destinations.take(4).forEachIndexed { i, destination -> NavigationBarItem(
                    selected = tab == i,
                    onClick = { tab = i },
                    icon = { Icon(destination.icon, null) },
                    label = { Text(if (i == 1) "Venta" else destination.title, maxLines = 1) }
                ) }
                NavigationBarItem(
                    selected = tab >= 4,
                    onClick = { scope.launch { drawer.open() } },
                    icon = { Icon(Icons.Default.GridView, null) },
                    label = { Text("Más") }
                )
            } },
            snackbarHost = { SnackbarHost(snackbar) }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceContainerLow)))) {
                Box(Modifier.widthIn(max = 1200.dp).fillMaxSize().align(Alignment.TopCenter).padding(horizontal = 16.dp)) {
                    when (tab) {
                        0 -> Dashboard(docs, expenses, products, categories, clients, payments, refunds) { tab = it }
                        1 -> SaleScreen(vm, products, categories, clients, company)
                        2 -> ProductsScreen(vm, products, categories)
                        3 -> ClientsScreen(vm, clients, docs, payments)
                        4 -> HistoryScreen(vm, docs, company)
                        5 -> InventoryScreen(vm, products, movements)
                        6 -> ExpensesScreen(vm, expenses)
                        7 -> CashScreen(vm, boxes, docs, payments, expenses, refunds)
                        8 -> SettingsScreen(vm, company, docs)
                    }
                }
                if (vm.busy) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)).clickable(onClick = {}), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
    }
    vm.receipt?.let { doc -> DocumentDialog(doc, company, { vm.receipt = null }, vm) }
    vm.paymentReceipt?.let { (client, list) ->
        val context = LocalContext.current
        AlertDialog(onDismissRequest = { vm.paymentReceipt = null }, title = { Text("Abono registrado") }, text = { Text("${client.nombre}\n${money(list.sumOf { it.monto })}\nSe generó el recibo de pago.") },
            confirmButton = { TextButton(onClick = { runCatching { Sharing.file(context, PdfGenerator.receipt(context, client, list), "application/pdf") }.onFailure { vm.error = it.message } }) { Text("Compartir PDF") } },
            dismissButton = { TextButton(onClick = { vm.paymentReceipt = null }) { Text("Cerrar") } })
    }
    vm.error?.let { error -> AlertDialog(onDismissRequest = { vm.error = null }, title = { Text("Revisa la operación") }, text = { Text(error) }, confirmButton = { TextButton(onClick = { vm.error = null }) { Text("Entendido") } }) }
}

@Composable
internal fun CardBlock(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (title != null) Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        content()
    } }
}
@Composable
internal fun LogoPreview(data: String?, modifier: Modifier = Modifier) {
    val bitmap = remember(data) { PdfGenerator.bitmap(data)?.asImageBitmap() }
    if (bitmap != null) Image(bitmap, "Logo del negocio", modifier, contentScale = ContentScale.Fit)
    else Image(painterResource(R.drawable.ic_launcher_foreground), "Tikete y Factura", modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)))
}
@Composable internal fun Empty(text: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Default.Inbox, null, Modifier.padding(14.dp).size(28.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
@Composable
internal fun Choices(options: List<String>, value: String, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option -> FilterChip(
            selected = value == option,
            onClick = { onChange(option) },
            label = { Text(option) },
            shape = RoundedCornerShape(12.dp),
            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = value == option, borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = Color.Transparent),
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer)
        ) }
    }
}
@Composable
internal fun Input(label: String, value: String, onChange: (String) -> Unit, numeric: Boolean = false, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value, onChange, modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text)
    )
}
internal data class Field(val key: String, val label: String, val value: String = "", val numeric: Boolean = false)
@Composable
internal fun FormDialog(title: String, fields: List<Field>, dismiss: () -> Unit, confirm: (Map<String, String>, () -> Unit) -> Unit) {
    val values = remember { mutableStateMapOf<String, String>().apply { fields.forEach { put(it.key, it.value) } } }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            fields.forEach { field -> Input(field.label, values[field.key].orEmpty(), { values[field.key] = it }, field.numeric) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onClick = { try { confirm(values.toMap(), dismiss) } catch (e: Exception) { error = e.message } }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
}
@Composable
internal fun Confirm(title: String, text: String, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss, title = { Text(title) }, text = { Text(text) }, confirmButton = { TextButton(onClick = confirm) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
}

@Composable
private fun Dashboard(docs: List<DocumentoConItems>, expenses: List<Gasto>, products: List<Producto>, categories: List<Categoria>, clients: List<Cliente>, payments: List<Abono>, refunds: List<Reembolso>, navigate: (Int) -> Unit) {
    var period by rememberSaveable { mutableStateOf("Hoy") }
    val today = LocalDate.now()
    val since = when (period) { "Semana" -> today.minusDays(6); "Mes" -> today.withDayOfMonth(1); else -> today }
    fun included(date: String) = if (period == "Todo") true else BusinessRules.datePart(date)?.let { !it.isBefore(since) && !it.isAfter(today) } == true
    val active = docs.filter { it.documento.estado != "ANULADA" && included(it.documento.fecha) }
    val sales = active.sumOf { it.documento.total }; val tax = active.sumOf { it.documento.impuesto }; val costs = active.sumOf { it.documento.costoTotal }
    val spending = expenses.filter { it.monto > 0 && included(it.fecha) }.sumOf { it.monto }
    val purchases = expenses.filter { it.monto > 0 && included(it.fecha) && BusinessRules.key(it.categoria) == BusinessRules.key("Mercancía") }.sumOf { it.monto }
    val collected = docs.filter { included(it.documento.fecha) }.filter { it.documento.metodoPago != "CRÉDITO" }.sumOf { it.documento.total } + payments.filter { included(it.fecha) }.sumOf { it.monto } - refunds.filter { included(it.fecha) }.sumOf { it.monto }
    val operating = spending - purchases
    val result = sales - tax - costs - operating
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
        item { Choices(listOf("Hoy", "Semana", "Mes", "Todo"), period) { period = it } }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ventas $period", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .72f), style = MaterialTheme.typography.labelLarge)
                            Text(money(sales), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineLarge)
                        }
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .12f)) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, Modifier.padding(13.dp).size(26.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Text("${active.size} operaciones · ${money(collected)} cobrado", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .78f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Resultado", money(result), Icons.Default.AutoGraph, result >= 0, Modifier.weight(1f))
            MetricCard("Por cobrar", money(clients.sumOf { it.deuda }), Icons.Default.Schedule, true, Modifier.weight(1f))
        } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("Nueva venta", "Crear cobro", Icons.Default.AddShoppingCart, true, { navigate(1) }, Modifier.weight(1f))
            QuickAction("Caja", "Abrir o cerrar", Icons.Default.AccountBalanceWallet, false, { navigate(7) }, Modifier.weight(1f))
        } }
        item { CardBlock("Cobros por método") { methods.filter { it != "CRÉDITO" }.forEach { m ->
            val value = docs.filter { included(it.documento.fecha) }.filter { it.documento.metodoPago == m }.sumOf { it.documento.total } + payments.filter { it.metodo == m && included(it.fecha) }.sumOf { it.monto } - refunds.filter { it.metodo == m && included(it.fecha) }.sumOf { it.monto }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(m.lowercase().replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(money(value), fontWeight = FontWeight.SemiBold) }
        } } }
        item { CardBlock("Ventas por día") {
            val groups = active.filter { BusinessRules.datePart(it.documento.fecha) != null }.groupBy { it.documento.fecha.take(10) }.toSortedMap().entries.toList().takeLast(7)
            val max = groups.maxOfOrNull { it.value.sumOf { doc -> doc.documento.total } }?.coerceAtLeast(1.0) ?: 1.0
            if (groups.isEmpty()) Empty("Aún no hay ventas con fecha completa en este período")
            groups.forEach { (day, entries) -> val total = entries.sumOf { it.documento.total }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(day, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(money(total), fontWeight = FontWeight.SemiBold) }; LinearProgressIndicator(progress = { (total / max).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(7.dp), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round, color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceContainerHigh) }
        } }
        item { CardBlock("Productos más vendidos") {
            val top = active.flatMap { it.items }.groupBy { it.productoId?.toString() ?: "antiguo:${it.nombre}" }.values.map { lines -> lines.first().nombre to lines.sumOf { it.cantidad } }.sortedByDescending { it.second }.take(5)
            if (top.isEmpty()) Empty("Aún no hay ventas")
            top.forEachIndexed { index, item -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) { Text("${index + 1}", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(10.dp)); Text(item.first, Modifier.weight(1f)); Text("${item.second}", fontWeight = FontWeight.SemiBold) } }
        } }
        item { CardBlock("Estadísticas de productos y servicios") {
            val quantities = active.flatMap { it.items }.filter { it.productoId != null }.groupBy { it.productoId!! }.mapValues { (_, lines) -> lines.sumOf { it.cantidad } }
            val categoryNames = categories.associate { it.id to it.nombre }
            fun kind(product: Producto): String = if (categoryNames[product.categoriaId]?.trim()?.lowercase()?.startsWith("servicio") == true) "Servicio" else "Producto"
            val sold = products.map { it to (quantities[it.id] ?: 0.0) }
            val best = sold.maxByOrNull { it.second }
            val least = sold.filter { it.second > 0.0 }.minByOrNull { it.second }
            if (best == null || best.second == 0.0) {
                Empty("Aún no hay ventas para calcular estas estadísticas")
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SalesStat("Más vendido", best.first.nombre, kind(best.first), best.first.unidad, best.second, Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    least?.let { SalesStat("Menos vendido", it.first.nombre, kind(it.first), it.first.unidad, it.second, Modifier.weight(1f), MaterialTheme.colorScheme.tertiary) }
                }
                val withoutSales = sold.count { it.second == 0.0 }
                if (withoutSales > 0) Text("$withoutSales artículo(s) del catálogo aún no tienen ventas en este período.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        } }
        item { CardBlock("Inventario bajo") {
            val low = products.filter { it.stock <= it.stockMinimo }
            if (low.isEmpty()) Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary); Spacer(Modifier.width(8.dp)); Text("Todo está en orden", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            low.forEach { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(it.nombre); Text("${it.stock} / mín. ${it.stockMinimo}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) } }
            TextButton(onClick = { navigate(5) }) { Text("Gestionar inventario") }
        } }
    }
}

@Composable
private fun SalesStat(title: String, name: String, type: String, unit: String, quantity: Double, modifier: Modifier, color: Color) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.10f)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
            Text(name, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("$type · ${formatQuantity(quantity, unit)} vendidos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, icon: ImageVector, positive: Boolean, modifier: Modifier = Modifier) {
    Card(modifier, shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = if (positive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun QuickAction(label: String, supporting: String, icon: ImageVector, primary: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = if (primary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        border = if (primary) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
