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
private const val PREMIUM_VERIFIED_AT = "premium_verified_at"
private const val PREMIUM_VALID_UNTIL = "premium_valid_until"
private val PREMIUM_OFFLINE_GRACE = TimeUnit.DAYS.toMillis(3)

/** Protege toda la aplicación después de los doce días de prueba. */
@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
internal fun SubscriptionGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configured = BuildConfig.REVENUECAT_GOOGLE_API_KEY.isNotBlank()
    var premiumActive by remember { mutableStateOf<Boolean?>(null) }
    var trialActive by remember { mutableStateOf<Boolean?>(null) }
    var accessResolved by remember { mutableStateOf(!configured) }
    var showPaywall by remember { mutableStateOf(false) }
    var restoring by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun applyCustomerInfo(customerInfo: CustomerInfo) {
        premiumActive = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
        savePremiumVerification(context, customerInfo)
        val serverTrialStartedAt = customerInfo.firstSeen.time
        saveTrialStartedAt(context, serverTrialStartedAt)
        val trustedNow = maxOf(System.currentTimeMillis(), customerInfo.requestDate.time)
        trialActive = trustedNow < serverTrialStartedAt +
            TimeUnit.DAYS.toMillis(BuildConfig.FREE_TRIAL_DAYS)
        accessResolved = true
    }

    fun refreshEntitlement() {
        if (!configured) return
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                applyCustomerInfo(customerInfo)
            }
            override fun onError(error: PurchasesError) {
                premiumActive = isStoredPremiumTrusted(context)
                trialActive = isStoredTrialActive(context)
                accessResolved = true
                message = if (premiumActive == true || trialActive == true) "No pudimos actualizar tu suscripción. Puedes seguir trabajando y volveremos a intentarlo después."
                else "No pudimos comprobar el acceso. Revisa tu conexión e inténtalo nuevamente."
            }
        })
    }
    LaunchedEffect(configured) { refreshEntitlement() }

    // Sin clave sólo se permite trabajar en builds de depuración. Un release falla cerrado.
    val permitted = premiumActive == true || trialActive == true || (BuildConfig.DEBUG && !configured)
    if (!accessResolved) SubscriptionLoadingScreen()
    else if (permitted) content() else SubscriptionLockedScreen(
        configured = configured,
        restoring = restoring,
        message = message,
        onBuy = { showPaywall = true },
        onRestore = {
            restoring = true
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    restoring = false
                    applyCustomerInfo(customerInfo)
                    message = if (premiumActive == true) "Compra restaurada" else "No se encontró un plan activo"
                }
                override fun onError(error: PurchasesError) { restoring = false; message = "No pudimos restaurar la compra. Revisa tu conexión y la cuenta de Google Play." }
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
private fun SubscriptionLoadingScreen() {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
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

private fun saveTrialStartedAt(context: Context, startedAt: Long) {
    val prefs = context.getSharedPreferences(TRIAL_PREFS, Context.MODE_PRIVATE)
    prefs.edit().putLong(TRIAL_STARTED_AT, startedAt).apply()
}

private fun savePremiumVerification(context: Context, customerInfo: CustomerInfo) {
    val entitlement = customerInfo.entitlements.active[PREMIUM_ENTITLEMENT]
    val verifiedAt = maxOf(System.currentTimeMillis(), customerInfo.requestDate.time)
    val validUntil = entitlement?.expirationDate?.time ?: if (entitlement != null) Long.MAX_VALUE else 0L
    context.getSharedPreferences(TRIAL_PREFS, Context.MODE_PRIVATE).edit()
        .putLong(PREMIUM_VERIFIED_AT, if (entitlement != null) verifiedAt else 0L)
        .putLong(PREMIUM_VALID_UNTIL, validUntil)
        .apply()
}

private fun isStoredPremiumTrusted(context: Context): Boolean {
    val prefs = context.getSharedPreferences(TRIAL_PREFS, Context.MODE_PRIVATE)
    val verifiedAt = prefs.getLong(PREMIUM_VERIFIED_AT, 0L)
    val validUntil = prefs.getLong(PREMIUM_VALID_UNTIL, 0L)
    if (verifiedAt <= 0L || validUntil <= 0L) return false
    val now = System.currentTimeMillis()
    return now <= validUntil && now - verifiedAt <= PREMIUM_OFFLINE_GRACE
}

private fun isStoredTrialActive(context: Context): Boolean {
    val startedAt = context.getSharedPreferences(TRIAL_PREFS, Context.MODE_PRIVATE)
        .getLong(TRIAL_STARTED_AT, 0L)
    if (startedAt <= 0L) return false
    return System.currentTimeMillis() <
        startedAt + TimeUnit.DAYS.toMillis(BuildConfig.FREE_TRIAL_DAYS)
}
