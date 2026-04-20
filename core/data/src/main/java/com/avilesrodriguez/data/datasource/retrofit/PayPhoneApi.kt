package com.avilesrodriguez.data.datasource.retrofit

import com.avilesrodriguez.domain.model.payments.PaymentRequest
import com.avilesrodriguez.domain.model.payments.PaymentResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PayPhoneApi {
    @POST("createPaymentLink")
    suspend fun createLink(@Body request: PaymentRequest): PaymentResponse
}