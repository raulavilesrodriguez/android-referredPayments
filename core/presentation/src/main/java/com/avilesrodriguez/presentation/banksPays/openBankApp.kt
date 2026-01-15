package com.avilesrodriguez.presentation.banksPays

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun openBankApp(packageName: String, context: Context){
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) {
        context.startActivity(intent)
    } else {
        // Si no está instalada, abrir Play Store
        val playStoreIntent =
            Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        context.startActivity(playStoreIntent)
    }
}