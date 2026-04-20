package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.retrofit.PayPhoneApi
import com.avilesrodriguez.domain.interfaces.IPaymentRepository
import com.avilesrodriguez.domain.model.payments.PaymentRequest
import com.avilesrodriguez.domain.model.payments.PaymentResponse
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val api: PayPhoneApi
) : IPaymentRepository {
    override suspend fun createPaymentLink(uid: String, amount: Double): Result<PaymentResponse> {
        return try {
            val response = api.createLink(PaymentRequest(uid, amount))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}