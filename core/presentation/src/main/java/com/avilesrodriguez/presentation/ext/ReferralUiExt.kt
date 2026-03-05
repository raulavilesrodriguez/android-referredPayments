package com.avilesrodriguez.presentation.ext

import androidx.compose.ui.graphics.Color
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.presentation.R

fun ReferralStatus.toColor(): Color {
    return when (this) {
        ReferralStatus.PENDING -> Color(0xFFF5AD18)
        ReferralStatus.PROCESSING -> Color(0xFF3674B5)
        ReferralStatus.REJECTED -> Color(0XFFD25353)
        ReferralStatus.PAID -> Color(0xFF609966)
    }
}

fun ReferralStatus.toDisplayName(): Int {
    return when (this) {
        ReferralStatus.PENDING -> R.string.pending
        ReferralStatus.PROCESSING -> R.string.processing
        ReferralStatus.REJECTED -> R.string.rejected
        ReferralStatus.PAID -> R.string.paid
    }
}

fun ReferralStatus.nameSelect(): Int {
    return when (this) {
        ReferralStatus.PROCESSING -> R.string.process
        ReferralStatus.REJECTED -> R.string.reject
        ReferralStatus.PAID -> R.string.pay
        else -> R.string.pending
    }
}

fun ReferralStatus.toDisplayIcon(): Int {
    return when (this) {
        ReferralStatus.PENDING -> R.drawable.sentiment_pending
        ReferralStatus.PROCESSING -> R.drawable.sentiment_processing
        ReferralStatus.REJECTED -> R.drawable.sentiment_rejected
        ReferralStatus.PAID -> R.drawable.sentiment_paid
    }
}

fun ReferralStatus.Companion.getById(id:Int): ReferralStatus?{
    if (id == R.string.all_status) return null
    return ReferralStatus.entries.find { option ->
        option.toDisplayName() == id
    } ?: ReferralStatus.PENDING
}

fun ReferralStatus.Companion.options(search: Boolean): List<Int>{
    if(search){
        return listOf(R.string.all_status) + ReferralStatus.entries.map { it.toDisplayName() }
    }
    return ReferralStatus.entries.map { it.toDisplayName() }
}