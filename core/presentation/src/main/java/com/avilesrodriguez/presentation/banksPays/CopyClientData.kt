package com.avilesrodriguez.presentation.banksPays

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context


fun copyClientData(
    context: Context,
    infoUser: String?
){
    val infoPay = infoUser?.trimIndent()?:""
    // Acceso al servicio de portapapeles del sistema Android
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("infoPay", infoPay)
    clipboard.setPrimaryClip(clip)
}