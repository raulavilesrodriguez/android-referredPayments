package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchReferralsByClient @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(namePrefix: String, currentUserId: String): Flow<List<Referral>> {
        return repository.searchReferralsByClient(namePrefix, currentUserId)
    }
}