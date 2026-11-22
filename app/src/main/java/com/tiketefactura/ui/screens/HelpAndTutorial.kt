package com.tiketefactura.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tiketefactura.BuildConfig

private const val ONBOARDING_PREFS = "onboarding"
private const val ONBOARDING_COMPLETED = "tutorial_completed"

internal fun tutorialWasCompleted(context: Context): Boolean =
    context.getSharedPreferences(ONBOARDING_PREFS, Context.MODE_PRIVATE)
        .getBoolean(ONBOARDING_COMPLETED, false)

internal fun completeTutorial(context: Context) {
    context.getSharedPreferences(ONBOARDING_PREFS, Context.MODE_PRIVATE)
        .edit().putBoolean(ONBOARDING_COMPLETED, true).apply()
}

private data class TutorialStep(val title: String, val description: String, val tip: String, val icon: ImageVector)

private val tutorialSteps = listOf(
    TutorialStep("Configura tu negocio", "En Ajustes agrega el nombre, logo, teléfonos, impuesto y formato de papel que aparecerán en tus comprobantes.", "Puedes cambiar estos datos cuando lo necesites.", Icons.Default.Storefront),
    TutorialStep("Prepara tu catálogo", "Crea categorías y productos, define precios y registra las existencias iniciales antes de comenzar a vender.", "Activa la venta sin stock solo si tu operación lo requiere.", Icons.Default.Inventory2),
    TutorialStep("Registra tu primera venta", "Abre Nueva venta, agrega productos, elige al cliente y selecciona el método de pago.", "Las ventas a crédito aparecerán en el estado de cuenta del cliente.", Icons.Default.PointOfSale),
    TutorialStep("Entrega el comprobante", "Al finalizar puedes imprimir el ticket, guardar un PDF o enviarlo por WhatsApp, correo u otra aplicación.", "Haz respaldos frecuentes desde Ajustes para proteger tu información.", Icons.Default.Share)
)

@Composable
internal fun TutorialDialog(onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val current = tutorialSteps[step]
    AlertDialog(
        onDismissRequest = {},
        icon = { Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) { Icon(current.icon, null, Modifier.padding(18.dp).size(38.dp), tint = MaterialTheme.colorScheme.primary) } },
        title = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Paso ${step + 1} de ${tutorialSteps.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Text(current.title, textAlign = TextAlign.Center) } },
        text = { Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(current.description, textAlign = TextAlign.Center)
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh) { Text(current.tip, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) }
            LinearProgressIndicator(progress = { (step + 1f) / tutorialSteps.size }, modifier = Modifier.fillMaxWidth())
        } },
        confirmButton = { Button(onClick = { if (step < tutorialSteps.lastIndex) step++ else onFinished() }) { Text(if (step == tutorialSteps.lastIndex) "Empezar" else "Siguiente") } },
        dismissButton = { if (step > 0) TextButton(onClick = { step-- }) { Text("Anterior") } else TextButton(onClick = onFinished) { Text("Omitir") } }
    )
}

private data class FrequentlyAskedQuestion(val question: String, val answer: String)
private val questions = listOf(
    FrequentlyAskedQuestion("¿Cómo registro una venta?", "Entra en Nueva venta, selecciona productos, cantidades, cliente y forma de pago. Revisa el total y confirma."),
    FrequentlyAskedQuestion("¿Cómo agrego productos?", "Abre Productos, crea las categorías que necesites y agrega cada producto con precio, costo, unidad y existencias."),
    FrequentlyAskedQuestion("¿Cómo configuro la impresora?", "Ve a Ajustes > Ventas y comprobantes > Configurar impresora Bluetooth. Enciende y vincula la impresora antes de buscarla."),
    FrequentlyAskedQuestion("¿Cómo comparto un comprobante?", "Abre Historial, selecciona el comprobante y toca Ver y enviar. Podrás usar WhatsApp, correo u otra aplicación."),
    FrequentlyAskedQuestion("¿Cómo hago un respaldo?", "En Ajustes > Respaldo y exportación toca Guardar respaldo completo. Conserva el archivo en un lugar seguro."),
    FrequentlyAskedQuestion("¿Cómo recupero una suscripción?", "En Ajustes > Suscripción y compras toca Restaurar compras con la misma cuenta de Google usada para pagar.")
)

@Composable
internal fun HelpAndCommunityCard() {
    val context = LocalContext.current
    var showTutorial by remember { mutableStateOf(false) }
    var showQuestions by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    CardBlock("Ayuda y comunidad") {
        Text("Aprende a usar la aplicación, solicita ayuda o compártela con otros negocios.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        HelpAction(Icons.Default.School, "Ver tutorial", "Repite la guía inicial paso a paso") { showTutorial = true }
        HelpAction(Icons.AutoMirrored.Filled.Help, "Preguntas frecuentes", "Ventas, productos, impresión y respaldos") { showQuestions = true }
        HelpAction(Icons.Default.Star, "Calificar en Google Play", "Deja una valoración y un comentario público") { openPlayStore(context) { message = it } }
        HelpAction(Icons.Default.Share, "Compartir aplicación", "Invita a otra persona a probarla") { shareApplication(context) }
        HorizontalDivider()
        Text("Tikete y Factura · versión ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
    }
    if (showTutorial) TutorialDialog { showTutorial = false }
    if (showQuestions) FrequentlyAskedQuestionsDialog { showQuestions = false }
}

@Composable
internal fun HelpScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 24.dp)
    ) {
        HelpAndCommunityCard()
    }
}

@Composable
private fun HelpAction(icon: ImageVector, title: String, description: String, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FrequentlyAskedQuestionsDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Preguntas frecuentes") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            questions.forEach { item ->
                var expanded by remember(item.question) { mutableStateOf(false) }
                Surface(onClick = { expanded = !expanded }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text(item.question, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null) }
                        if (expanded) Text(item.answer, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

private fun openPlayStore(context: Context, onError: (String) -> Unit) {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
    val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
    runCatching { context.startActivity(market) }.recoverCatching { context.startActivity(web) }.onFailure { onError("No se pudo abrir Google Play.") }
}

private fun shareApplication(context: Context) {
    val url = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, "Tikete y Factura"); putExtra(Intent.EXTRA_TEXT, "Te recomiendo Tikete y Factura para administrar ventas, inventario y comprobantes.\n$url") }
    context.startActivity(Intent.createChooser(intent, "Compartir Tikete y Factura"))
}
