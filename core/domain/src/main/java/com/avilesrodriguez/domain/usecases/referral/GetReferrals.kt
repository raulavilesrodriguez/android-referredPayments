package com.avilesrodriguez.domain.usecases.referral

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import javax.inject.Inject

class GetReferrals @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(
        userId: String,
        pageSize: Long,
        namePrefix: String,
        status: String?,
        lastReferral: Referral?,
        isClient: Boolean
    ) : Pair<List<Referral>, Referral?> {
        return repository.getReferrals(userId, pageSize, namePrefix, status, lastReferral, isClient)
    }
}