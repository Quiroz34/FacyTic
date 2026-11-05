package com.tiketefactura.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiketefactura.data.*
import com.tiketefactura.data.local.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CartItem(val producto: Producto, val cantidad: Double)

@HiltViewModel
class MainViewModel @Inject constructor(val repository: BusinessRepository) : ViewModel() {
    val db = repository.db
    val productos = db.productoDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val clientes = db.clienteDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categorias = db.categoriaDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val documentos = db.documentoDao().getAllWithItems().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val empresa = db.empresaDao().get().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val gastos = db.gastoDao().getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val movimientos = db.inventarioDao().getRecientes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val abonos = db.operacionesDao().abonos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val reembolsos = db.operacionesDao().reembolsos().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cajas = db.operacionesDao().cajas().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    var busy by mutableStateOf(false); private set
    var message by mutableStateOf<String?>(null)
    var error by mutableStateOf<String?>(null)
    var cart by mutableStateOf<List<CartItem>>(emptyList()); private set
    var receipt by mutableStateOf<DocumentoConItems?>(null)
    var paymentReceipt by mutableStateOf<Pair<Cliente, List<Abono>>?>(null)
    var ready by mutableStateOf(false); private set
    var startupError by mutableStateOf<String?>(null); private set
    init { load() }
    fun load() {
        startupError = null
        viewModelScope.launch {
            val started = System.currentTimeMillis()
            try {
                withContext(Dispatchers.IO) { db.openHelper.writableDatabase }
                delay((900 - (System.currentTimeMillis() - started)).coerceAtLeast(0))
                ready = true
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { startupError = "No fue posible abrir los datos. Se conservaron sin borrarlos. ${e.message.orEmpty()}" }
        }
    }
    fun action(success: String = "Guardado", after: () -> Unit = {}, block: suspend () -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try { withContext(Dispatchers.IO) { block() }; after(); message = success }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = e.message ?: "No se pudo completar la operación" }
            finally { busy = false }
        }
    }
    fun add(p: Producto) {
        val step = if (p.unidad == BusinessRepository.UNIT_PIECE) 1.0 else 0.1
        quantity(p, (cart.find { it.producto.id == p.id }?.cantidad ?: 0.0) + step)
    }
    fun quantity(p: Producto, qty: Double) {
        if (!qty.isFinite() || qty < 0) { message = "Cantidad inválida"; return }
        val fresh = productos.value.find { it.id == p.id } ?: p
        if (fresh.unidad == BusinessRepository.UNIT_PIECE && qty != kotlin.math.floor(qty)) { message = "${p.nombre} se vende por piezas completas"; return }
        if (empresa.value?.permitirSinStock != true && qty > fresh.stock) { message = "Stock insuficiente: ${p.nombre}"; return }
        cart = cart.filter { it.producto.id != p.id } + if (qty > 0) listOf(CartItem(fresh, qty)) else emptyList()
    }
    fun clear() { cart = emptyList() }
    fun refreshCart(products: List<Producto>) {
        if (busy || cart.isEmpty()) return
        cart = cart.map { item -> products.find { it.id == item.producto.id }?.let { item.copy(producto = it) } ?: item }
    }
    fun sale(client: String, clientId: Int?, type: String, method: String, discount: Double, tax: Boolean, cash: Double, signature: String?, after: () -> Unit) {
        val lines = cart.map { SaleLine(it.producto.id, it.cantidad) }
        var saved: DocumentoConItems? = null
        action("Venta guardada", after = { receipt = saved; clear(); after() }) {
            saved = repository.sale(lines, client, clientId, type, method, discount, tax, cash, signature)
        }
    }
}
