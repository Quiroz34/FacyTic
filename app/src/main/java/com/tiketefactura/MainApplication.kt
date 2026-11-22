package com.tiketefactura

import android.app.Application
import android.provider.Settings
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import java.security.MessageDigest

@HiltAndroidApp
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val apiKey = BuildConfig.REVENUECAT_GOOGLE_API_KEY
        if (apiKey.isNotBlank()) {
            if (BuildConfig.DEBUG) Purchases.logLevel = LogLevel.DEBUG
            val builder = PurchasesConfiguration.Builder(this, apiKey)
            stableRevenueCatUserId()?.let { builder.appUserID(it) }
            Purchases.configure(builder.build())
        }
    }

    /** RevenueCat conserva firstSeen para este hash aunque la app se reinstale. */
    private fun stableRevenueCatUserId(): String? {
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?.takeIf { it.isNotBlank() }
            ?: return null
        val source = "$packageName:$androidId"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        return "android_$hash"
    }
}
