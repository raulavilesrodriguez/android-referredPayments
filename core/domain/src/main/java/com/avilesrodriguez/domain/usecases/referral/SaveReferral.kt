package com.avilesrodriguez.domain.usecases.referral

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import javax.inject.Inject

class SaveReferral @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(referral: Referral) : Boolean {
        return repository.saveReferral(referral)
    }
}