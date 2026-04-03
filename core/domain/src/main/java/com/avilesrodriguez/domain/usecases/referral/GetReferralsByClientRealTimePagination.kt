package com.avilesrodriguez.domain.usecases.referral

import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReferralsByClientRealTimePagination @Inject constructor(
    private val repository: IReferralRepository
) {
    suspend operator fun invoke(clientId: String, limit: Long, status: String?) : Flow<List<Referral>>{
        return repository.getReferralsByClientRealTimePagination(clientId, limit, status)
    }
}