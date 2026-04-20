package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.payments.PaymentResponse

interface IPaymentRepository {
    suspend fun createPaymentLink(uid: String, amount: Double): Result<PaymentResponse>
}