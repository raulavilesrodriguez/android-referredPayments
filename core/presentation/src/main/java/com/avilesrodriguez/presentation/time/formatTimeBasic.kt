package com.avilesrodriguez.presentation.time

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun formatTimeBasic(millis: Long?) : String {
    return remember(millis) {
        val date = java.util.Date(millis ?: System.currentTimeMillis())
        // dd/MM/yyyy -> Fecha (día/mes/año)
        // hh:mm -> Hora y minutos (formato 12h)
        // a -> Marcador AM/PM
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        
        // Formateamos para obtener "AM/PM" en mayúsculas
        sdf.format(date)
    }
}