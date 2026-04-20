package com.avilesrodriguez.domain.usecases.payments

import com.avilesrodriguez.domain.interfaces.IPaymentRepository
import com.avilesrodriguez.domain.model.payments.PaymentResponse
import javax.inject.Inject

class CreatePaymentLinkUseCase @Inject constructor(
    private val repository: IPaymentRepository
) {
    suspend operator fun invoke(uid: String, amount: Double): Result<PaymentResponse> {
        if(amount <= 0) return Result.failure(Exception("Invalid amount"))
        return repository.createPaymentLink(uid, amount)
    }
}