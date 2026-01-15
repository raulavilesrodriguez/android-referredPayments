package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReferralsByProvider @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(providerId: String): Flow<List<Referral>> {
        return repository.getReferralsByProvider(providerId)
    }
}