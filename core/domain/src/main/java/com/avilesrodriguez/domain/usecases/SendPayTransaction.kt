package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.ITransactionsRepository
import com.avilesrodriguez.domain.model.message.Message
import jakarta.inject.Inject

class SendPayTransaction @Inject constructor(
    private val repository: ITransactionsRepository
) {
    suspend operator fun invoke(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        clientUid: String,
        providerUid: String,
        amountPaid: Double
    ){
        repository.sendPayTransaction(referralId, referralUpdates, message, clientUid, providerUid, amountPaid)
    }
}