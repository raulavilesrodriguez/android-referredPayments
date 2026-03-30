package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow

interface IReferralRepository {
    suspend fun saveReferral(referral: Referral): Boolean
    suspend fun updateReferralFields(referralId: String, updates: Map<String, Any>)
    suspend fun getReferralsByClient(clientId: String): Flow<List<Referral>>
    suspend fun getReferralsByProvider(providerId: String): Flow<List<Referral>>
    suspend fun getReferralsByClientByProvider(clientId: String, providerId: String): Flow<List<Referral>>
    suspend fun getReferralById(referralId: String): Referral?
    suspend fun getReferralByIdFlow(id: String): Flow<Referral?>
    suspend fun updateReferralStatus(referralId: String, status: String, voucherUrl: String?)
    suspend fun searchReferralsByClient(namePrefix: String, currentUserId: String): Flow<List<Referral>>
    suspend fun searchReferralsByProvider(namePrefix: String, currentUserId: String): Flow<List<Referral>>
    suspend fun searchReferralsByClientAndProvider(namePrefix: String, clientId: String, providerId: String): Flow<List<Referral>>
    suspend fun saveRatingWithTransaction(referralId: String, referralUpdates: Map<String, Any>, providerId: String, ratingReferral: Double)
    suspend fun getReferralsByClientSince(clientId: String, since: Long, isPaymentsScreen: Boolean) : Flow<List<Referral>>
    suspend fun getReferralsByProviderSince(providerId: String, since: Long, isPaymentsScreen: Boolean) : Flow<List<Referral>>
    suspend fun getReferralsByClientPaged(clientId: String, pageSize: Long, lastReferral: Referral?, fromDate: Long?, toDate: Long?, status: String?, isPaymentsScreen: Boolean) : Pair<List<Referral>, Referral?>
    suspend fun getReferralsByProviderPaged(providerId: String, pageSize: Long, lastReferral: Referral?, fromDate: Long?, toDate: Long?, status: String?, isPaymentsScreen: Boolean) : Pair<List<Referral>, Referral?>
}