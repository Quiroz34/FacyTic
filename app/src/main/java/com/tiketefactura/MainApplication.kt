package com.tiketefactura

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val apiKey = BuildConfig.REVENUECAT_GOOGLE_API_KEY
        if (apiKey.isNotBlank()) {
            if (BuildConfig.DEBUG) Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(PurchasesConfiguration.Builder(this, apiKey).build())
        }
    }
}
