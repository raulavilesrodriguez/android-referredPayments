package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import javax.inject.Inject

class GetReferralsByProviderPaged @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(providerId: String, pageSize: Long, lastReferral: Referral?) : Pair<List<Referral>, Referral?>{
        return repository.getReferralsByProviderPaged(providerId, pageSize, lastReferral)
    }
}