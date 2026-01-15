package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow

interface IReferralRepository {
    suspend fun saveReferral(referral: Referral)
    suspend fun getReferralsByClient(clientId: String): Flow<List<Referral>>
    suspend fun getReferralsByProvider(providerId: String): Flow<List<Referral>>
    suspend fun updateReferralStatus(referralId: String, status: String, voucherUrl: String?)
    suspend fun searchReferralsByClient(namePrefix: String, currentUserId: String): Flow<List<Referral>>
    suspend fun searchReferralsByProvider(namePrefix: String, currentUserId: String): Flow<List<Referral>>
}