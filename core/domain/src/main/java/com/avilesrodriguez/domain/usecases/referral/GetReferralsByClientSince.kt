package com.avilesrodriguez.domain.usecases.referral

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReferralsByClientSince @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(clientId: String, since: Long, status: String?, isPaymentsScreen: Boolean) : Flow<List<Referral>> {
        return repository.getReferralsByClientSince(clientId, since, status, isPaymentsScreen)
    }
}