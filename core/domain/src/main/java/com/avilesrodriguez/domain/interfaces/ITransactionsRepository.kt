package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.message.Message

interface ITransactionsRepository {
    suspend fun sendPayTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        clientUid: String,
        providerUid: String,
        amountPaid: Double
    )
    suspend fun rejectReferralTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        providerUid: String
    )
}