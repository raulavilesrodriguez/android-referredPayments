package com.avilesrodriguez.domain.model.payments

data class Payments(
    val id: String = "",
    val shortId: String = "",
    val uid: String = "",
    val amount: Double = 0.0,
    val authorizationCode: String = "",
    val date: Long = System.currentTimeMillis(),
    val status: String
)
