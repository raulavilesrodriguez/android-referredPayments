package com.avilesrodriguez.domain.model.referral

import androidx.compose.ui.graphics.Color

data class Referral(
    val id: String = "",
    val clientId: String = "",      // Quién lo refirió
    val providerId: String = "",    // A qué empresa se refirió
    val name: String = "",
    val nameLowercase: String = "",
    val email: String = "",
    val numberPhone: String = "",
    val status: ReferralStatus = ReferralStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val voucherUrl: String? = null  // la foto del pago que sube el Provider
)
