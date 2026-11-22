package com.tiketefactura.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.tiketefactura.BuildConfig
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

internal const val PREMIUM_ENTITLEMENT = "premium"

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
internal fun SubscriptionCard() {
    val context = LocalContext.current
    val configured = BuildConfig.REVENUECAT_GOOGLE_API_KEY.isNotBlank()
    var active by remember { mutableStateOf<Boolean?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var trialDaysRemaining by remember { mutableStateOf<Int?>(null) }

    fun refresh() {
        if (!configured) return
        busy = true
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                active = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
                val trustedNow = maxOf(System.currentTimeMillis(), customerInfo.requestDate.time)
                val remainingMillis = customerInfo.firstSeen.time + TimeUnit.DAYS.toMillis(BuildConfig.FREE_TRIAL_DAYS) - trustedNow
                trialDaysRemaining = ceil(remainingMillis.coerceAtLeast(0L).toDouble() / TimeUnit.DAYS.toMillis(1).toDouble()).toInt()
                busy = false
            }

            override fun onError(error: PurchasesError) {
                message = "No pudimos actualizar el estado. Revisa tu conexión e inténtalo nuevamente."
                busy = false
            }
        })
    }

    LaunchedEffect(configured) { refresh() }

    CardBlock("Suscripción y compras") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (active == true) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Icon(
                    if (active == true) Icons.Default.CheckCircle else Icons.Default.WorkspacePremium,
                    null,
                    Modifier.padding(12.dp).size(26.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when {
                        !configured -> "Compras no configuradas"
                        active == true -> "Plan Premium activo"
                        active == false -> "Plan gratuito"
                        else -> "Consultando suscripción…"
                    },
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (active == true) "Tu acceso Premium está disponible en este dispositivo."
                    else trialDaysRemaining?.takeIf { it > 0 }?.let { "Te quedan $it día(s) de prueba gratuita." }
                        ?: "Elige un plan para continuar usando todas las funciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        }

        if (!configured) {
            Text(
                "Falta agregar la clave pública de Google Play para mostrar planes y restaurar compras.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (active == true) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
                onClick = {
                    val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        .onFailure { message = "No se pudo abrir la administración de Google Play." }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, null)
                Spacer(Modifier.width(8.dp))
                Text("Administrar suscripción")
            }
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
                onClick = { message = null; showPaywall = true }
            ) {
                Icon(Icons.Default.WorkspacePremium, null)
                Spacer(Modifier.width(8.dp))
                Text("Ver planes disponibles")
            }
            Text("Google Play mostrará los planes y precios disponibles para tu país antes de confirmar.", style = MaterialTheme.typography.bodySmall)
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = configured && !busy,
            onClick = {
                message = null
                busy = true
                Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        busy = false
                        active = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
                        message = if (active == true) "Compra restaurada correctamente."
                        else "No se encontró una suscripción activa en esta cuenta de Google."
                    }

                    override fun onError(error: PurchasesError) {
                        busy = false
                        message = "No pudimos restaurar la compra. Revisa tu conexión y confirma que usas la misma cuenta de Google Play."
                    }
                })
            }
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text(if (busy) "Comprobando compras…" else "Restaurar compras")
        }
        Text(
            "Restaura si ya pagaste con esta cuenta de Google en otro dispositivo o después de reinstalar la app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        message?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = if (active == true) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showPaywall && configured) {
        PaywallDialog(
            PaywallDialogOptions.Builder()
                .setRequiredEntitlementIdentifier(PREMIUM_ENTITLEMENT)
                .setShouldDisplayBlock { true }
                .setDismissRequest { showPaywall = false; refresh() }
                .build()
        )
    }
}
