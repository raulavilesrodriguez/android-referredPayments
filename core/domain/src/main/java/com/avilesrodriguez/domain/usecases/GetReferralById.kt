package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import javax.inject.Inject

class GetReferralById @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(referralId: String): Referral?{
        return repository.getReferralById(referralId)
    }
}