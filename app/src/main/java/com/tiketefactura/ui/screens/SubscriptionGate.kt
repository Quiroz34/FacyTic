package com.tiketefactura.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

private const val TRIAL_PREFS = "subscription_access"
private const val TRIAL_STARTED_AT = "trial_started_at"

/** Protege toda la aplicación después de los cinco días de prueba. */
@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
internal fun SubscriptionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configured = BuildConfig.REVENUECAT_GOOGLE_API_KEY.isNotBlank()
    val trialStartedAt = remember { trialStartedAt(context) }
    var premiumActive by remember { mutableStateOf<Boolean?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var restoring by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val remainingMs = (trialStartedAt + TimeUnit.DAYS.toMillis(BuildConfig.FREE_TRIAL_DAYS) - System.currentTimeMillis()).coerceAtLeast(0)
    val trialActive = remainingMs > 0

    fun refreshEntitlement() {
        if (!configured) return
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                premiumActive = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
            }
            override fun onError(error: PurchasesError) {
                premiumActive = false
                message = error.message
            }
        })
    }
    LaunchedEffect(configured) { refreshEntitlement() }

    // Sin clave sólo se permite trabajar en builds de depuración. Un release falla cerrado.
    val permitted = premiumActive == true || trialActive || (BuildConfig.DEBUG && !configured)
    if (permitted) content() else SubscriptionLockedScreen(
        configured = configured,
        restoring = restoring,
        message = message,
        onBuy = { showPaywall = true },
        onRestore = {
            restoring = true
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    restoring = false
                    premiumActive = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
                    message = if (premiumActive == true) "Compra restaurada" else "No se encontró un plan activo"
                }
                override fun onError(error: PurchasesError) { restoring = false; message = error.message }
            })
        }
    )

    if (showPaywall && configured) {
        PaywallDialog(
            PaywallDialogOptions.Builder()
                .setRequiredEntitlementIdentifier(PREMIUM_ENTITLEMENT)
                .setShouldDisplayBlock { true }
                .setDismissRequest { showPaywall = false; refreshEntitlement() }
                .build()
        )
    }
}

@Composable
private fun SubscriptionLockedScreen(configured: Boolean, restoring: Boolean, message: String?, onBuy: () -> Unit, onRestore: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Terminó tu prueba gratuita", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("Para seguir usando Tikete y Factura, elige tu plan Premium.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Button(enabled = configured && !restoring, onClick = onBuy, modifier = Modifier.fillMaxWidth()) { Text("Elegir plan mensual o anual") }
            OutlinedButton(enabled = configured && !restoring, onClick = onRestore, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Restaurar compras") }
            if (!configured) Text("La facturación aún no está configurada. Contacta a soporte.", Modifier.padding(top = 18.dp), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            message?.let { Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) }
        }
    }
}

private fun trialStartedAt(context: Context): Long {
    val prefs = context.getSharedPreferences(TRIAL_PREFS, Context.MODE_PRIVATE)
    val stored = prefs.getLong(TRIAL_STARTED_AT, 0L)
    if (stored > 0L) return stored
    return System.currentTimeMillis().also { prefs.edit().putLong(TRIAL_STARTED_AT, it).apply() }
}
