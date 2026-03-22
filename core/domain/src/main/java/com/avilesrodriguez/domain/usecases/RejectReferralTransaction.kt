package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.ITransactionsRepository
import com.avilesrodriguez.domain.model.message.Message
import javax.inject.Inject

class RejectReferralTransaction @Inject constructor(
    private val repository: ITransactionsRepository
) {
    suspend operator fun invoke(
        referralId: String,
        referralUpdates: Map<String, Any>,
        message: Message,
        providerUid: String
    ){
        repository.rejectReferralTransaction(referralId, referralUpdates, message, providerUid)
    }
}