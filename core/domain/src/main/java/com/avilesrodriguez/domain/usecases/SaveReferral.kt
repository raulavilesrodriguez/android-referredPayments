package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import javax.inject.Inject

class SaveReferral @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(referral: Referral) {
        repository.saveReferral(referral)
    }
}