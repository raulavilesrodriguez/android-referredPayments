package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import javax.inject.Inject

class UpdateReferralStatus @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(referralId: String, status: String, voucherUrl: String){
        repository.updateReferralStatus(referralId, status, voucherUrl)
    }
}