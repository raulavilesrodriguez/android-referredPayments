package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import javax.inject.Inject

class UpdateReferralFields @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(referralId: String, updates: Map<String, Any>) {
        repository.updateReferralFields(referralId, updates)
    }
}