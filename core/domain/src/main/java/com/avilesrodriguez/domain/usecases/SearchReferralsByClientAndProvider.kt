package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchReferralsByClientAndProvider @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(
        namePrefix: String,
        clientId: String,
        providerId: String
    ): Flow<List<Referral>>{
        return repository.searchReferralsByClientAndProvider(namePrefix, clientId, providerId)
    }
}