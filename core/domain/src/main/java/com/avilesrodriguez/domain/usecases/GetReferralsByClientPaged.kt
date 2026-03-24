package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import javax.inject.Inject

class GetReferralsByClientPaged @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(
        clientId: String,
        pageSize: Long,
        lastReferral: Referral?,
        fromDate: Long?,
        toDate: Long?,
        status: String?,
        isPaymentsScreen: Boolean
    ) : Pair<List<Referral>, Referral?>{
        return repository.getReferralsByClientPaged(clientId, pageSize, lastReferral, fromDate, toDate, status, isPaymentsScreen)
    }
}