package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.TransactionsDataSource
import com.avilesrodriguez.domain.interfaces.ITransactionsRepository
import com.avilesrodriguez.domain.model.message.Message
import javax.inject.Inject

class TransactionsRepository @Inject constructor(
    private val data: TransactionsDataSource
): ITransactionsRepository {
    override suspend fun sendPayTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        clientUid: String,
        providerUid: String,
        amountPaid: Double
    ) {
        data.sendPayTransaction(referralId, referralUpdates, message, clientUid, providerUid, amountPaid)
    }

    override suspend fun rejectReferralTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        providerUid: String
    ) {
        data.rejectReferralTransaction(referralId, referralUpdates, message, providerUid)
    }
}