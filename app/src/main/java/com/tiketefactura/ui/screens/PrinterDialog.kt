package com.tiketefactura.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tiketefactura.data.local.*
import com.tiketefactura.util.*

@Composable
internal fun PrinterDialog(vm: MainViewModel, doc: DocumentoConItems?, company: Empresa?, dismiss: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("thermal-printer", 0) }
    var devices by remember { mutableStateOf<List<PairedPrinter>>(emptyList()) }
    var selected by remember { mutableStateOf(preferences.getString("address", "").orEmpty()) }
    var paper by remember { mutableStateOf(preferences.getString("paper", "58").orEmpty()) }
    var legacy by remember { mutableStateOf(preferences.getBoolean("legacy", false)) }
    var error by remember { mutableStateOf<String?>(null) }
    fun refresh() { runCatching { BluetoothPrinter.paired(context) }.onSuccess { devices = it; error = if (it.isEmpty()) "Vincula la impresora desde los ajustes Bluetooth del teléfono" else null }.onFailure { error = it.message } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) refresh() else error = "Para conectar la impresora, permite Dispositivos cercanos en los ajustes de esta app" }
    val settings = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { if (BluetoothPrinter.permitted(context)) refresh() }
    LaunchedEffect(Unit) { if (BluetoothPrinter.permitted(context)) refresh() }
    AlertDialog(onDismissRequest = { if (!vm.busy) dismiss() }, title = { Text("Impresora térmica") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Conecta una impresora Bluetooth clásico compatible con ESC/POS. Las impresoras BLE con aplicación propia requieren confirmar el modelo.")
            Row { TextButton(onClick = { if (BluetoothPrinter.permitted(context)) refresh() else permission.launch(Manifest.permission.BLUETOOTH_CONNECT) }) { Text("Actualizar lista") }
                TextButton(onClick = { settings.launch(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }) { Text("Vincular") } }
            devices.forEach { device -> Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected == device.address, onClick = { selected = device.address }, enabled = device.classic)
                Column { Text(device.name); Text(if (device.classic) device.address.takeLast(8) else "BLE: confirma el modelo", style = MaterialTheme.typography.bodySmall) }
            } }
            Choices(listOf("58", "80"), paper) { paper = it }; Text("Ancho del papel en milímetros")
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Conexión para modelos antiguos", Modifier.weight(1f)); Switch(legacy, { legacy = it }) }
            Text("Usa esta opción solo si la conexión normal falla. No cambia el formato del ticket.", style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(enabled = selected.isNotBlank() && !vm.busy, onClick = {
                val address = selected; val width = paper; val mode = legacy
                val test = DocumentoConItems(Documento(tipo = "PRUEBA", numero = "PRUEBA-BLUETOOTH", fecha = com.tiketefactura.data.BusinessRules.now(), clienteNombre = "Prueba de impresión", subtotal = 1.0, total = 1.0, efectivoRecibido = 1.0),
                    listOf(DocumentoItem(documentoId = 0, nombre = "QZ Web Solutions · áéíóú ñ", cantidad = 1.0, precio = 1.0, subtotal = 1.0)))
                vm.action("Ticket de prueba enviado. Confirma la salida en la impresora.") {
                    val file = PdfGenerator.create(context, test, company, width)
                    BluetoothPrinter.print(context, address, file, if (width == "58") 384 else 576, mode)
                }
            }) { Text("Imprimir prueba") }
        }
    }, confirmButton = { TextButton(enabled = selected.isNotBlank() && !vm.busy, onClick = {
        val address = selected; val width = paper; val mode = legacy
        preferences.edit().putString("address", address).putString("paper", width).putBoolean("legacy", mode).apply()
        if (doc == null) { vm.message = "Impresora guardada"; dismiss() }
        else vm.action("Ticket enviado. Comprueba la salida en la impresora.", after = dismiss) {
            val file = PdfGenerator.create(context, doc, company, width)
            BluetoothPrinter.print(context, address, file, if (width == "58") 384 else 576, mode)
        }
    }) { Text(if (doc == null) "Guardar" else "Imprimir ticket") } }, dismissButton = { TextButton(enabled = !vm.busy, onClick = dismiss) { Text("Cerrar") } })
}
