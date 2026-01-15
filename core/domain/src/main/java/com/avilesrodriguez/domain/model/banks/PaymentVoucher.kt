package com.avilesrodriguez.domain.model.banks

import java.util.Date

data class PaymentVoucher(
    val paymentId: String,
    val referralId: String,
    val providerId: String,
    val clientId: String,
    val amount: Double,
    val voucherUrl: String, // URL de la imagen en Firebase Storage
    val status: String = "VERIFIED", // El provider dice que ya pagó
    val createdAt: Date = Date()
)
