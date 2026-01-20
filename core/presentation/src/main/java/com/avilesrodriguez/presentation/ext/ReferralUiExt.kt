package com.avilesrodriguez.presentation.ext

import androidx.compose.ui.graphics.Color
import com.avilesrodriguez.domain.model.referral.ReferralStatus

fun ReferralStatus.toColor(): Color {
    return when (this) {
        ReferralStatus.PENDING -> Color(0xFFF5AD18)
        ReferralStatus.COMPLETED -> Color(0xFF6594B1)
        ReferralStatus.REJECTED -> Color(0XFFDC0E0E)
        ReferralStatus.PAID -> Color(0xFF08CB00)
    }
}

fun ReferralStatus.toDisplayName(): String {
    return when (this) {
        ReferralStatus.PENDING -> this.name
        ReferralStatus.COMPLETED -> this.name
        ReferralStatus.REJECTED -> this.name
        ReferralStatus.PAID -> this.name
    }
}