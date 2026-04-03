package com.avilesrodriguez.domain.usecases.referral

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReferralByIdFlow @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(id: String): Flow<Referral?> = repository.getReferralByIdFlow(id)
}