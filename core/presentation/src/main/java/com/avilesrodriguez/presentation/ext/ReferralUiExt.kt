package com.avilesrodriguez.presentation.ext

import androidx.compose.ui.graphics.Color
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.presentation.R

fun ReferralStatus.toColor(): Color {
    return when (this) {
        ReferralStatus.PENDING -> Color(0xFFF5AD18)
        ReferralStatus.PROCESSING -> Color(0xFF6594B1)
        ReferralStatus.REJECTED -> Color(0XFFDC0E0E)
        ReferralStatus.PAID -> Color(0xFF08CB00)
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