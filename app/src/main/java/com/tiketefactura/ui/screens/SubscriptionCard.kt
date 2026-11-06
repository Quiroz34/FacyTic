package com.tiketefactura.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.tiketefactura.BuildConfig

internal const val PREMIUM_ENTITLEMENT = "premium"

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
internal fun SubscriptionCard() {
    val configured = BuildConfig.REVENUECAT_GOOGLE_API_KEY.isNotBlank()
    var active by remember { mutableStateOf<Boolean?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        if (!configured) return
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                active = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
            }
            override fun onError(error: PurchasesError) {
                message = error.message
                active = false
            }
        })
    }

    LaunchedEffect(configured) { refresh() }

    CardBlock("Suscripción") {
        Text(
            when {
                !configured -> "RevenueCat está listo, pero falta agregar la clave pública de Google Play."
                active == true -> "Plan Premium activo"
                active == false -> "Sin suscripción activa"
                else -> "Consultando suscripción…"
            }
        )
        Text("Mensual: $85 MXN · Anual: $900 MXN", style = MaterialTheme.typography.bodySmall)
        Button(enabled = configured && !busy, onClick = { showPaywall = true }) {
            Text(if (active == true) "Administrar plan" else "Ver planes")
        }
        OutlinedButton(enabled = configured && !busy, onClick = {
            busy = true
            Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    busy = false
                    active = customerInfo.entitlements.active.containsKey(PREMIUM_ENTITLEMENT)
                    message = if (active == true) "Compra restaurada" else "No se encontró una suscripción activa"
                }
                override fun onError(error: PurchasesError) {
                    busy = false
                    message = error.message
                }
            })
        }) { Text("Restaurar compras") }
        message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
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
