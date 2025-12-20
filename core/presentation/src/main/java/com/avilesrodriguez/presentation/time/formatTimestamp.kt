package com.avilesrodriguez.presentation.time

import android.icu.util.Calendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Locale
import com.avilesrodriguez.presentation.R

@Composable
fun formatTimestamp(millis: Long?) : String {
    val yesterdayString = stringResource(R.string.yesterday)

    return remember(millis, yesterdayString) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis ?: System.currentTimeMillis()
        val todayCalendar = Calendar.getInstance()

        if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)) {
            // Si es hoy, muestra solo la hora y minutos.
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date(calendar.timeInMillis))
        } else {
            todayCalendar.add(Calendar.DAY_OF_YEAR, -1)
            if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)) {
                yesterdayString
            } else {
                // convert millis (Long) to date "03/05/2025"
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(java.util.Date(calendar.timeInMillis))
            }
        }
    }
}