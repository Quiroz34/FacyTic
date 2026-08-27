package com.tiketefactura.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.tiketefactura.data.local.*
import com.tiketefactura.ui.theme.*
import com.tiketefactura.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// --- DATA ---
data class CartItem(val producto: Producto, var cantidad: Double)

// --- VIEWMODEL ---
@HiltViewModel
class MainViewModel @Inject constructor(
    private val productoDao: ProductoDao,
    private val empresaDao: EmpresaDao,
    private val documentoDao: DocumentoDao,
    private val categoriaDao: CategoriaDao,
    private val clienteDao: ClienteDao,
    private val gastoDao: GastoDao
) : ViewModel() {
    val productos = productoDao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val empresa = empresaDao.get().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val documentos = documentoDao.getAllWithItems().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categorias = categoriaDao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val clientes = clienteDao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val gastos = gastoDao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesByDay = documentos.map { list ->
        list.groupBy { it.documento.fecha.split(",")[0] }
            .mapValues { entry -> entry.value.sumOf { it.documento.total } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    fun addToCart(p: Producto) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.producto.id == p.id }
        if (index >= 0) current[index] = current[index].copy(cantidad = current[index].cantidad + 1)
        else current.add(CartItem(p, 1.0))
        _cart.value = current
    }
    fun removeFromCart(p: Producto) { _cart.value = _cart.value.filter { it.producto.id != p.id } }
    fun clearCart() { _cart.value = emptyList() }

    fun addProducto(n: String, p: Double, c: Int?, s: Double) { 
        viewModelScope.launch { productoDao.insert(Producto(nombre = n, precio = p, categoriaId = c, stock = s)) } 
    }
    fun updateProducto(p: Producto) { viewModelScope.launch { productoDao.update(p) } }
    fun deleteProducto(p: Producto) { viewModelScope.launch { productoDao.delete(p) } }
    
    fun addCategoria(n: String) { viewModelScope.launch { categoriaDao.insert(Categoria(nombre = n)) } }
    fun updateCategoria(c: Categoria) { viewModelScope.launch { categoriaDao.update(c) } }
    fun deleteCategoria(c: Categoria) { viewModelScope.launch { categoriaDao.delete(c) } }

    fun addCliente(n: String, t: String?, e: String?, d: String?) { viewModelScope.launch { clienteDao.insert(Cliente(nombre = n, telefono = t, correo = e, direccion = d)) } }
    fun deleteCliente(c: Cliente) { viewModelScope.launch { clienteDao.delete(c) } }

    fun addGasto(desc: String, monto: Double, cat: String) {
        viewModelScope.launch {
            val fecha = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
            gastoDao.insert(Gasto(descripcion = desc, monto = monto, fecha = fecha, categoria = cat))
        }
    }
    fun deleteGasto(g: Gasto) { viewModelScope.launch { gastoDao.delete(g) } }

    fun updateEmpresa(n: String, r: String, d: String, t: String, s: String, m: String, impActivo: Boolean, impPorc: Double, logo: String?) { 
        viewModelScope.launch { empresaDao.insert(Empresa(nombre = n, ruc = r, direccion = d, telefono = t, eslogan = s, mensajePie = m, impuestoActivo = impActivo, impuestoPorcentaje = impPorc, logoUri = logo)) } 
    }

    fun exportToCSV(context: Context) {
        viewModelScope.launch {
            val docs = documentos.value
            val csv = StringBuilder("Numero,Tipo,Fecha,Cliente,Total,Pago\n")
            docs.forEach { d ->
                csv.append("${d.documento.numero},${d.documento.tipo},${d.documento.fecha},${d.documento.clienteNombre},${d.documento.total},${d.documento.metodoPago}\n")
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Historial de Ventas")
                putExtra(Intent.EXTRA_TEXT, csv.toString())
            }
            context.startActivity(Intent.createChooser(intent, "Exportar Historial"))
        }
    }

    fun saveDocumento(tipo: String, cliente: String, clienteId: Int?, metodo: String, aplicarImpuesto: Boolean) {
        viewModelScope.launch {
            val items = _cart.value
            if (items.isEmpty()) return@launch
            items.forEach { productoDao.update(it.producto.copy(stock = it.producto.stock - it.cantidad)) }
            val subtotal = items.sumOf { it.producto.precio * it.cantidad }
            val impPorc = empresa.value?.impuestoPorcentaje ?: 15.0
            val impuesto = if (aplicarImpuesto) subtotal * (impPorc / 100.0) else 0.0
            val doc = Documento(
                tipo = tipo, numero = "TKT-${System.currentTimeMillis().toString().takeLast(5)}",
                fecha = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date()),
                clienteNombre = cliente, clienteId = clienteId, subtotal = subtotal, impuesto = impuesto, total = subtotal + impuesto,
                metodoPago = metodo
            )
            documentoDao.saveFullDocumento(doc, items.map { DocumentoItem(documentoId = 0, nombre = it.producto.nombre, cantidad = it.cantidad, precio = it.producto.precio, subtotal = it.producto.precio * it.cantidad) })
            clearCart()
        }
    }
}

// --- UI PRINCIPAL ---
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    var currentTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val productos by viewModel.productos.collectAsState()
    val empresa by viewModel.empresa.collectAsState()
    val documentos by viewModel.documentos.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val clientes by viewModel.clientes.collectAsState()
    val gastos by viewModel.gastos.collectAsState()
    val salesData by viewModel.salesByDay.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(280.dp).fillMaxHeight()
            ) {
                Spacer(Modifier.statusBarsPadding())
                Spacer(Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                    if (empresa?.logoUri != null) {
                        AsyncImage(model = empresa?.logoUri, contentDescription = null, modifier = Modifier.size(56.dp).clip(CircleShape))
                    } else {
                        Box(modifier = Modifier.size(56.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Store, null, tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(empresa?.nombre ?: "Mi Negocio", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(32.dp))
                
                val menuItems = listOf(
                    Triple(Icons.Default.GridView, "Dashboard", 0),
                    Triple(Icons.Default.Inventory2, "Productos", 1),
                    Triple(Icons.Default.AddCircle, "Vender", 2),
                    Triple(Icons.Default.People, "Clientes", 5),
                    Triple(Icons.Default.Payments, "Finanzas", 6),
                    Triple(Icons.AutoMirrored.Filled.ReceiptLong, "Historial", 3),
                    Triple(Icons.Default.Settings, "Configuración", 4)
                )

                menuItems.forEach { (icon, label, index) ->
                    NavigationDrawerItem(
                        label = { Text(label, fontWeight = FontWeight.Medium) },
                        selected = currentTab == index,
                        onClick = {
                            currentTab = index
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(icon, null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = Primary.copy(alpha = 0.1f), selectedIconColor = Primary, selectedTextColor = Primary, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary)
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Background)) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp)).background(Surface, RoundedCornerShape(12.dp))) {
                        Icon(Icons.Default.Menu, null, tint = Primary)
                    }
                    Text(text = "TIKETE & FACTURA", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = TextMuted, letterSpacing = 2.sp)
                    Spacer(Modifier.width(48.dp))
                }

                Box(modifier = Modifier.weight(1f).widthIn(max = 500.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                    AnimatedContent(targetState = currentTab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "MainContent") { tab ->
                        when (tab) {
                            0 -> Dashboard(empresa, documentos, productos, salesData, gastos) { currentTab = it }
                            1 -> Catalogo(productos, categorias, viewModel)
                            2 -> PuntoDeVenta(productos, categorias, clientes, viewModel)
                            3 -> Historial(documentos, empresa)
                            4 -> ConfigScreen(viewModel)
                            5 -> ClientesScreen(clientes, viewModel)
                            6 -> FinanzasScreen(gastos, viewModel)
                        }
                    }
                }
            }
        }
    }
}

// --- PANTALLAS ---

@Composable
fun Dashboard(empresa: Empresa?, documentos: List<DocumentoConItems>, productos: List<Producto>, salesData: Map<String, Double>, gastos: List<Gasto>, onNavigate: (Int) -> Unit) {
    val totalVendido = documentos.sumOf { it.documento.total }
    val totalGastos = gastos.sumOf { it.monto }
    val utilidad = totalVendido - totalGastos
    val lowStock = productos.filter { it.stock < 10 }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Column {
                Text("Bienvenido,", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text(empresa?.nombre ?: "Mi Negocio", color = TextPrimary, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
            }
        }

        item {
            PremiumCard(modifier = Modifier.fillMaxWidth(), containerColor = Primary) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("UTILIDAD REAL", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                    Text("$${String.format("%.2f", utilidad)}", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(16.dp))
                    SalesMiniChart(salesData)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatBentoCard("Ventas", "$${String.format("%.2f", totalVendido)}", Icons.AutoMirrored.Filled.TrendingUp, Success, Modifier.weight(1f))
                StatBentoCard("Egresos", "$${String.format("%.2f", totalGastos)}", Icons.AutoMirrored.Filled.TrendingDown, Error, Modifier.weight(1f))
            }
        }

        if (lowStock.isNotEmpty()) {
            item {
                Text("ALERTAS DE STOCK", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
                lowStock.take(2).forEach { p ->
                    Surface(color = Error.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Error.copy(alpha = 0.1f)), modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(p.nombre, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("${p.stock.toInt()} disp.", color = Error, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        item {
            Text("ACCIONES RÁPIDAS", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickActionIcon(Icons.Default.AddShoppingCart, "Vender", Primary) { onNavigate(2) }
                QuickActionIcon(Icons.Default.Payments, "Gasto", Error) { onNavigate(6) }
                QuickActionIcon(Icons.Default.History, "Historial", Warning) { onNavigate(3) }
                QuickActionIcon(Icons.Default.Settings, "Ajustes", Info) { onNavigate(4) }
            }
        }
    }
}

@Composable
fun SalesMiniChart(data: Map<String, Double>) {
    val values = data.values.toList().takeLast(7)
    val max = values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    Row(modifier = Modifier.fillMaxWidth().height(60.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (values.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text("Iniciando registros...", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
            }
        } else {
            values.forEach { value ->
                val heightFactor = (value / max).toFloat().coerceIn(0.1f, 1f)
                Box(modifier = Modifier.weight(1f).fillMaxHeight(heightFactor).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color.White.copy(alpha = 0.3f)))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Catalogo(productos: List<Producto>, categorias: List<Categoria>, viewModel: MainViewModel) {
    var showAddProduct by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var showManageCats by remember { mutableStateOf(false) }
    var editingProducto by remember { mutableStateOf<Producto?>(null) }
    val grouped = remember(productos, categorias) { productos.groupBy { p -> categorias.find { it.id == p.categoriaId }?.nombre ?: "Otros" } }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Catálogo", color = TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showAddCategory = true }, modifier = Modifier.background(Surface, RoundedCornerShape(12.dp)).border(1.dp, Border, RoundedCornerShape(12.dp))) { Icon(Icons.Default.CreateNewFolder, null, tint = Secondary) }
                IconButton(onClick = { showManageCats = true }, modifier = Modifier.background(Surface, RoundedCornerShape(12.dp)).border(1.dp, Border, RoundedCornerShape(12.dp))) { Icon(Icons.Default.FolderSpecial, null, tint = TextSecondary) }
                IconButton(onClick = { showAddProduct = true }, modifier = Modifier.background(Primary, RoundedCornerShape(12.dp))) { Icon(Icons.Default.Add, null, tint = Color.White) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            grouped.forEach { (cat, items) ->
                stickyHeader { Box(modifier = Modifier.fillMaxWidth().background(Background)) { Text(cat.uppercase(), color = TextMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(vertical = 12.dp)) } }
                items(items) { p -> 
                    PremiumCard(modifier = Modifier.clickable { editingProducto = p }) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.nombre, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("$${String.format("%.2f", p.precio)} • Stock: ${p.stock.toInt()}", color = if(p.stock < 10) Error else Primary, fontSize = 13.sp)
                            }
                            IconButton(onClick = { viewModel.deleteProducto(p) }) { Icon(Icons.Default.DeleteOutline, null, tint = Error.copy(alpha = 0.5f)) }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Border)
                        }
                    }
                }
            }
        }
    }
    if (showAddProduct) AddProductDialog(categorias, onDismiss = { showAddProduct = false }, onConfirm = { n, p, c, s -> viewModel.addProducto(n, p, c, s); showAddProduct = false }, onAddNewCategory = { showAddCategory = true })
    if (editingProducto != null) AddProductDialog(categorias, editingProducto, onDismiss = { editingProducto = null }, onConfirm = { n, p, c, s -> viewModel.updateProducto(editingProducto!!.copy(nombre = n, precio = p, categoriaId = c, stock = s)); editingProducto = null }, onAddNewCategory = { showAddCategory = true })
    if (showAddCategory) AddCategoryDialog(onDismiss = { showAddCategory = false }, onConfirm = { viewModel.addCategoria(it); showAddCategory = false })
    if (showManageCats) ManageCategoriesDialog(categorias, onDismiss = { showManageCats = false }, viewModel = viewModel)
}

@Composable
fun PuntoDeVenta(productos: List<Producto>, categorias: List<Categoria>, clientes: List<Cliente>, viewModel: MainViewModel) {
    val cart by viewModel.cart.collectAsState()
    val empresa by viewModel.empresa.collectAsState()
    
    var clienteNombre by remember { mutableStateOf("") }
    var selectedClienteId by remember { mutableStateOf<Int?>(null) }
    var tipo by remember { mutableStateOf("TICKET") }
    var metodoPago by remember { mutableStateOf("EFECTIVO") }
    var aplicarIVA by remember { mutableStateOf(empresa?.impuestoActivo ?: true) }
    
    var showSelector by remember { mutableStateOf(false) }
    var showClienteSelector by remember { mutableStateOf(false) }
    var showSignature by remember { mutableStateOf(false) }
    var saleCompletedDoc by remember { mutableStateOf<DocumentoConItems?>(null) }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Venta Nueva", color = TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        ModernInput(value = clienteNombre, onValueChange = { clienteNombre = it; selectedClienteId = null }, label = "Cliente", modifier = Modifier.padding(top = 16.dp), trailingIcon = { IconButton(onClick = { showClienteSelector = true }) { Icon(Icons.Default.PersonSearch, null, tint = Primary) } })
        Row(modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth().background(Surface, RoundedCornerShape(12.dp)).padding(4.dp)) {
            listOf("TICKET", "FACTURA").forEach { t ->
                val sel = tipo == t
                Box(modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(8.dp)).background(if(sel) (if(t=="TICKET") Secondary else Primary) else SurfaceLight).clickable { tipo = t }, contentAlignment = Alignment.Center) {
                    Text(t, color = if(sel) Color.White else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("MÉTODO:", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(8.dp))
            listOf("EFECTIVO", "TARJETA").forEach { m ->
                val sel = metodoPago == m
                Box(modifier = Modifier.padding(end = 4.dp).clip(RoundedCornerShape(8.dp)).background(if(sel) Primary.copy(alpha = 0.1f) else Color.Transparent).border(1.dp, if(sel) Primary else Border, RoundedCornerShape(8.dp)).clickable { metodoPago = m }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(m, color = if(sel) Primary else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("IVA", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Switch(checked = aplicarIVA, onCheckedChange = { aplicarIVA = it })
        }
        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cart) { item ->
                PremiumCard {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.producto.nombre, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("${item.cantidad}x $${String.format("%.2f", item.producto.precio)}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Text("$${String.format("%.2f", item.producto.precio * item.cantidad)}", color = TextPrimary, fontWeight = FontWeight.Black)
                        IconButton(onClick = { viewModel.removeFromCart(item.producto) }) { Icon(Icons.Default.RemoveCircleOutline, null, tint = TextMuted) }
                    }
                }
            }
            item {
                OutlinedButton(onClick = { showSelector = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Search, null); Spacer(Modifier.width(8.dp)); Text("Añadir Productos")
                }
            }
        }
        val subtotal = cart.sumOf { it.producto.precio * it.cantidad }
        val total = subtotal + (if(aplicarIVA) subtotal * ((empresa?.impuestoPorcentaje ?: 15.0)/100.0) else 0.0)
        Button(
            onClick = { if(cart.isNotEmpty()) showSignature = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if(tipo=="FACTURA") Primary else Secondary)
        ) { Text("COBRAR - $${String.format("%.2f", total)}", fontSize = 15.sp, fontWeight = FontWeight.Black) }
    }
    if (showSelector) ProductSelectorDialog(productos, categorias, onDismiss = { showSelector = false }) { viewModel.addToCart(it); showSelector = false }
    if (showSignature) {
        SignatureDialog(
            onDismiss = { showSignature = false },
            onConfirm = { 
                val subtotal = cart.sumOf { it.producto.precio * it.cantidad }
                val total = subtotal + (if(aplicarIVA) subtotal * ((empresa?.impuestoPorcentaje ?: 15.0)/100.0) else 0.0)
                val currentItems = cart.map { DocumentoItem(documentoId = 0, nombre = it.producto.nombre, cantidad = it.cantidad, precio = it.producto.precio, subtotal = it.producto.precio * it.cantidad) }
                val tempDoc = Documento(tipo = tipo, numero = "TKT-${System.currentTimeMillis().toString().takeLast(4)}", fecha = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date()), clienteNombre = clienteNombre.ifBlank { "Consumidor Final" }, total = total, metodoPago = metodoPago)
                saleCompletedDoc = DocumentoConItems(tempDoc, currentItems)
                viewModel.saveDocumento(tipo, clienteNombre.ifBlank { "Consumidor Final" }, selectedClienteId, metodoPago, aplicarIVA)
                clienteNombre = ""; selectedClienteId = null; showSignature = false
            }
        )
    }
    if (saleCompletedDoc != null) {
        AlertDialog(onDismissRequest = { saleCompletedDoc = null }, containerColor = Surface, icon = { Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(48.dp)) }, title = { Text("Venta Exitosa", fontWeight = FontWeight.Black) }, text = { Text("¿Deseas imprimir o compartir el comprobante?") },
            confirmButton = { Button(onClick = { PdfGenerator.generateAndPrint(context, saleCompletedDoc!!, empresa); saleCompletedDoc = null }) { Text("Enviar / Imprimir") } },
            dismissButton = { TextButton(onClick = { saleCompletedDoc = null }) { Text("Cerrar") } }
        )
    }
    if (showClienteSelector) {
        Dialog(onDismissRequest = { showClienteSelector = false }) {
            PremiumCard(modifier = Modifier.fillMaxHeight(0.7f)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Seleccionar Cliente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(clientes) { c -> Row(modifier = Modifier.fillMaxWidth().clickable { clienteNombre = c.nombre; selectedClienteId = c.id; showClienteSelector = false }.background(SurfaceLight, RoundedCornerShape(12.dp)).padding(16.dp)) { Text(c.nombre, color = TextPrimary, fontWeight = FontWeight.Bold) } }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientesScreen(clientes: List<Cliente>, viewModel: MainViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Clientes", color = TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            IconButton(onClick = { showAdd = true }, modifier = Modifier.background(Primary, RoundedCornerShape(12.dp))) { Icon(Icons.Default.PersonAdd, null, tint = Color.White) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(clientes) { c ->
                PremiumCard {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(c.nombre, color = TextPrimary, fontWeight = FontWeight.Bold)
                            c.telefono?.let { Text(it, color = TextSecondary, fontSize = 13.sp) }
                        }
                        IconButton(onClick = { viewModel.deleteCliente(c) }) { Icon(Icons.Default.Delete, null, tint = Error) }
                    }
                }
            }
        }
    }
    if (showAdd) {
        var n by remember { mutableStateOf("") }
        var t by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAdd = false }) {
            PremiumCard {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Nuevo Cliente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    ModernInput(value = n, onValueChange = { n = it }, label = "Nombre")
                    ModernInput(value = t, onValueChange = { t = it }, label = "Teléfono")
                    Button(onClick = { if(n.isNotBlank()) viewModel.addCliente(n, t, null, null); showAdd = false }, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Guardar") }
                }
            }
        }
    }
}

@Composable
fun FinanzasScreen(gastos: List<Gasto>, viewModel: MainViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Gastos", color = TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            IconButton(onClick = { showAdd = true }, modifier = Modifier.background(Error, RoundedCornerShape(12.dp))) { Icon(Icons.Default.Add, null, tint = Color.White) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(gastos) { g ->
                PremiumCard {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(g.descripcion, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("${g.fecha} • ${g.categoria}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$${String.format("%.2f", g.monto)}", color = Error, fontWeight = FontWeight.Black)
                            IconButton(onClick = { viewModel.deleteGasto(g) }) { Icon(Icons.Default.Delete, null, tint = Border, modifier = Modifier.size(16.dp)) }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) {
        var d by remember { mutableStateOf("") }
        var m by remember { mutableStateOf("") }
        var c by remember { mutableStateOf("Mercancía") }
        Dialog(onDismissRequest = { showAdd = false }) {
            PremiumCard {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Registrar Gasto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    ModernInput(value = d, onValueChange = { d = it }, label = "Descripción")
                    ModernInput(value = m, onValueChange = { m = it }, label = "Monto")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Mercancía", "Servicios", "Renta").forEach { cat ->
                            val sel = c == cat
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if(sel) Error.copy(alpha = 0.1f) else Color.Transparent).border(1.dp, if(sel) Error else Border, RoundedCornerShape(8.dp)).clickable { c = cat }.padding(4.dp), contentAlignment = Alignment.Center) {
                                Text(cat, fontSize = 10.sp, color = if(sel) Error else TextSecondary)
                            }
                        }
                    }
                    Button(onClick = { if(d.isNotBlank()) viewModel.addGasto(d, m.toDoubleOrNull() ?: 0.0, c); showAdd = false }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Guardar Gasto") }
                }
            }
        }
    }
}

@Composable
fun SignatureDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var paths by remember { mutableStateOf(mutableListOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        PremiumCard(modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("FIRMA DEL CLIENTE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceLight).border(1.dp, Border, RoundedCornerShape(12.dp)).pointerInput(Unit) { detectDragGestures(onDragStart = { offset -> currentPath = Path().apply { moveTo(offset.x, offset.y) }; currentPath?.let { paths.add(it) } }, onDrag = { change, _ -> currentPath?.lineTo(change.position.x, change.position.y); val temp = paths.toMutableList(); paths = temp }) }) {
                    Canvas(modifier = Modifier.fillMaxSize()) { paths.forEach { path -> drawPath(path = path, color = Primary, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) } }
                }
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { paths = mutableListOf() }, modifier = Modifier.weight(1f)) { Text("Limpiar") }
                    Button(onClick = { onConfirm("") }, modifier = Modifier.weight(1f)) { Text("Confirmar") }
                }
            }
        }
    }
}

@Composable
fun Historial(documentos: List<DocumentoConItems>, empresa: Empresa?) {
    var selected by remember { mutableStateOf<DocumentoConItems?>(null) }
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Historial", color = TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(documentos) { d ->
                PremiumCard(modifier = Modifier.clickable { selected = d }) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(d.documento.numero, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("${d.documento.clienteNombre} • ${d.documento.metodoPago}", color = TextSecondary, fontSize = 13.sp)
                            Text(d.documento.fecha, color = TextMuted, fontSize = 11.sp)
                        }
                        Text("$${String.format("%.2f", d.documento.total)}", color = if(d.documento.tipo=="FACTURA") Primary else Secondary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }
        }
    }
    selected?.let { doc ->
        AlertDialog(onDismissRequest = { selected = null }, containerColor = Background, title = { Text(doc.documento.numero, color = TextPrimary, fontWeight = FontWeight.Bold) }, text = { Column { doc.items.forEach { Text("${it.cantidad}x ${it.nombre} ($${String.format("%.2f", it.subtotal)})", color = TextSecondary) } } }, confirmButton = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = { selected = null }) { Text("Cerrar") }; Button(onClick = { PdfGenerator.generateAndPrint(context, doc, empresa); selected = null }) { Text("Imprimir / PDF") } } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: MainViewModel = hiltViewModel()) {
    val empresa by viewModel.empresa.collectAsState()
    var n by remember { mutableStateOf("") }
    var r by remember { mutableStateOf("") }
    var d by remember { mutableStateOf("") }
    var t by remember { mutableStateOf("") }
    var s by remember { mutableStateOf("") }
    var m by remember { mutableStateOf("") }
    var iActivo by remember { mutableStateOf(true) }
    var iPorc by remember { mutableStateOf("15") }
    var logoUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> logoUri = uri?.toString() }

    LaunchedEffect(empresa) { empresa?.let { n = it.nombre; r = it.ruc ?: ""; d = it.direccion ?: ""; t = it.telefono ?: ""; s = it.eslogan ?: ""; m = it.mensajePie ?: ""; iActivo = it.impuestoActivo; iPorc = it.impuestoPorcentaje.toString(); logoUri = it.logoUri } }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Text("IDENTIDAD VISUAL", color = TextMuted, style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceLight).clickable { launcher.launch("image/*") }, contentAlignment = Alignment.Center) {
                    if (logoUri != null) AsyncImage(model = logoUri, contentDescription = null, contentScale = ContentScale.Crop)
                    else Icon(Icons.Default.AddPhotoAlternate, null, tint = TextMuted)
                }
                Spacer(Modifier.width(16.dp))
                Column { Text("Logo", fontWeight = FontWeight.Bold, color = TextPrimary); Text("Toca para cambiar", fontSize = 12.sp, color = Primary) }
            }
        }
        item { ModernInput(value = n, onValueChange = { n = it }, label = "Nombre") }
        item { ModernInput(value = r, onValueChange = { r = it }, label = "RUC / NIT") }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Cobrar IVA", modifier = Modifier.weight(1f), color = TextPrimary); Switch(checked = iActivo, onCheckedChange = { iActivo = it }) } }
        if (iActivo) { item { ModernInput(value = iPorc, onValueChange = { iPorc = it }, label = "% IVA") } }
        item { ModernInput(value = t, onValueChange = { t = it }, label = "Teléfono") }
        item { ModernInput(value = s, onValueChange = { s = it }, label = "Eslogan") }
        item { ModernInput(value = d, onValueChange = { d = it }, label = "Dirección") }
        item { ModernInput(value = m, onValueChange = { m = it }, label = "Mensaje al pie") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = { viewModel.exportToCSV(context) }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Success)) { Icon(Icons.Default.Download, null); Text("Excel") }; Button(onClick = { viewModel.updateEmpresa(n, r, d, t, s, m, iActivo, iPorc.toDoubleOrNull() ?: 0.0, logoUri) }, modifier = Modifier.weight(1.5f).height(52.dp), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Save, null); Text("Guardar") } } }
    }
}

// --- COMPONENTES ---
@Composable
fun PremiumCard(modifier: Modifier = Modifier, containerColor: Color = Surface, content: @Composable () -> Unit) { Surface(modifier = modifier, color = containerColor, shape = RoundedCornerShape(20.dp), border = if (containerColor == Surface) BorderStroke(1.dp, Border) else null, shadowElevation = 2.dp, content = content) }

@Composable
fun StatBentoCard(label: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier) { PremiumCard(modifier = modifier) { Column(modifier = Modifier.padding(20.dp)) { Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp)); Spacer(Modifier.height(12.dp)); Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall); Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black) } } }

@Composable
fun QuickActionIcon(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp).clickable { onClick() }) { Box(modifier = Modifier.size(56.dp).background(color.copy(alpha = 0.1f), CircleShape).border(BorderStroke(1.dp, color.copy(alpha = 0.2f)), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }; Spacer(Modifier.height(4.dp)); Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold) } }

@Composable
fun ModernInput(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, trailingIcon: @Composable (() -> Unit)? = null) { Column(modifier = modifier) { Text(label.uppercase(), color = TextMuted, style = MaterialTheme.typography.labelSmall); Spacer(modifier = Modifier.height(6.dp)); OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), trailingIcon = trailingIcon, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedContainerColor = Surface, unfocusedContainerColor = Surface, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)) } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(categorias: List<Categoria>, existingProducto: Producto? = null, onDismiss: () -> Unit, onConfirm: (String, Double, Int?, Double) -> Unit, onAddNewCategory: () -> Unit) {
    var n by remember { mutableStateOf(existingProducto?.nombre ?: "") }
    var p by remember { mutableStateOf(existingProducto?.precio?.toString() ?: "") }
    var s by remember { mutableStateOf(existingProducto?.stock?.toString() ?: "0") }
    var cid by remember { mutableStateOf(existingProducto?.categoriaId) }
    var exp by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) { PremiumCard { Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(if(existingProducto==null)"Nuevo Producto" else "Editar", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); ModernInput(value = n, onValueChange = { n = it }, label = "Nombre"); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ModernInput(value = p, onValueChange = { p = it }, label = "Precio", modifier = Modifier.weight(1f)); ModernInput(value = s, onValueChange = { s = it }, label = "Stock", modifier = Modifier.weight(1f)) }; Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = it }, modifier = Modifier.weight(1f)) { OutlinedTextField(value = categorias.find { it.id == cid }?.nombre ?: "Sin Sección", onValueChange = {}, readOnly = true, label = { Text("Sección") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exp) }, modifier = Modifier.menuAnchor(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)); ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }, modifier = Modifier.background(Surface)) { DropdownMenuItem(text = { Text("Sin Sección") }, onClick = { cid = null; exp = false }); categorias.forEach { c -> DropdownMenuItem(text = { Text(c.nombre) }, onClick = { cid = c.id; exp = false }) } } }; IconButton(onClick = onAddNewCategory, modifier = Modifier.padding(top = 20.dp).background(Surface, RoundedCornerShape(8.dp)).border(BorderStroke(1.dp, Border), RoundedCornerShape(8.dp))) { Icon(Icons.Default.Add, null, tint = Primary) } }; Button(onClick = { onConfirm(n, p.toDoubleOrNull()?:0.0, cid, s.toDoubleOrNull() ?: 0.0); onDismiss() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Guardar") }; TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cerrar", color = TextMuted) } } } }
}

@Composable
fun AddCategoryDialog(existing: Categoria? = null, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var n by remember { mutableStateOf(existing?.nombre ?: "") }
    Dialog(onDismissRequest = onDismiss) { PremiumCard { Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) { Text("Nueva Sección", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); ModernInput(value = n, onValueChange = { n = it }, label = "Nombre"); Button(onClick = { if(n.isNotBlank()) onConfirm(n); onDismiss() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("Aceptar") }; TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancelar", color = TextMuted) } } } }
}

@Composable
fun ManageCategoriesDialog(categorias: List<Categoria>, onDismiss: () -> Unit, viewModel: MainViewModel) {
    Dialog(onDismissRequest = onDismiss) { PremiumCard { Column(modifier = Modifier.padding(32.dp).fillMaxHeight(0.6f)) { Text("Secciones", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Spacer(modifier = Modifier.height(24.dp)); LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) { items(categorias) { c -> Row(modifier = Modifier.fillMaxWidth().background(SurfaceLight, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(c.nombre, color = TextPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold); IconButton(onClick = { viewModel.deleteCategoria(c) }) { Icon(Icons.Default.Delete, null, tint = Error) } } } }; Button(onClick = { onDismiss() }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(12.dp)) { Text("Cerrar") } } } }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductSelectorDialog(productos: List<Producto>, categorias: List<Categoria>, onDismiss: () -> Unit, onSelect: (Producto) -> Unit) {
    val grouped = remember(productos, categorias) { productos.groupBy { p -> categorias.find { it.id == p.categoriaId }?.nombre ?: "Sin Sección" } }
    Dialog(onDismissRequest = onDismiss) { PremiumCard(modifier = Modifier.fillMaxHeight(0.8f)) { Column(modifier = Modifier.padding(24.dp)) { Text("Seleccionar", color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black); Spacer(modifier = Modifier.height(16.dp)); LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) { grouped.forEach { (cat, items) -> stickyHeader { Box(modifier = Modifier.fillMaxWidth().background(Surface)) { Text(cat.uppercase(), color = TextMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(vertical = 4.dp)) } }; items(items) { p -> Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceLight).clickable { onSelect(p) }.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(p.nombre, color = TextPrimary, fontWeight = FontWeight.Bold); Text("$${String.format("%.2f", p.precio)}", color = Primary, fontWeight = FontWeight.Black) } } } }; TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) { Text("Cancelar") } } } }
}
