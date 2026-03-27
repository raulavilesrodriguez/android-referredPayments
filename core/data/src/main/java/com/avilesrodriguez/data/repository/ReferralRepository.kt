package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.ReferralDataSource
import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.model.referral.Referral
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReferralRepository @Inject constructor(
    private val data: ReferralDataSource
): IReferralRepository {

    override suspend fun saveReferral(referral: Referral): Boolean = data.saveReferral(referral)

    override suspend fun updateReferralFields(referralId: String, updates: Map<String, Any>) {
        data.updateReferralFields(referralId, updates)
    }

    override suspend fun getReferralsByClient(clientId: String): Flow<List<Referral>> {
        return data.getReferralsByClient(clientId)
    }

    override suspend fun getReferralsByProvider(providerId: String): Flow<List<Referral>> {
        return data.getReferralsByProvider(providerId)
    }

    override suspend fun getReferralsByClientByProvider(
        clientId: String,
        providerId: String
    ): Flow<List<Referral>> {
        return data.getReferralsByClientByProvider(clientId, providerId)
    }

    override suspend fun getReferralById(referralId: String): Referral? {
        return data.getReferralById(referralId)
    }

    override suspend fun getReferralByIdFlow(id: String): Flow<Referral?> {
        return data.getReferralByIdFlow(id)
    }

    override suspend fun updateReferralStatus(
        referralId: String,
        status: String,
        voucherUrl: String?
    ) {
        data.updateReferralStatus(referralId, status, voucherUrl)
    }

    override suspend fun searchReferralsByClient(
        namePrefix: String,
        currentUserId: String
    ): Flow<List<Referral>> {
        return data.searchReferralsByClient(namePrefix, currentUserId)
    }

    override suspend fun searchReferralsByProvider(
        namePrefix: String,
        currentUserId: String
    ): Flow<List<Referral>> {
        return data.searchReferralsByProvider(namePrefix, currentUserId)
    }

    override suspend fun searchReferralsByClientAndProvider(
        namePrefix: String,
        clientId: String,
        providerId: String
    ): Flow<List<Referral>> {
        return data.searchReferralsByClientAndProvider(namePrefix, clientId, providerId)
    }

    override suspend fun saveRatingWithTransaction(
        referralId: String,
        referralUpdates: Map<String, Any>,
        providerId: String,
        ratingReferral: Double
    ) {
        data.saveRatingWithTransaction(referralId, referralUpdates, providerId, ratingReferral)
    }

    override suspend fun getReferralsByClientSince(
        clientId: String,
        since: Long,
        toDate: Long?,
        isPaymentsScreen: Boolean
    ): Flow<List<Referral>> {
        return data.getReferralsByClientSince(clientId, since, toDate, isPaymentsScreen)
    }

    override suspend fun getReferralsByProviderSince(
        providerId: String,
        since: Long,
        toDate: Long?,
        isPaymentsScreen: Boolean
    ): Flow<List<Referral>> {
        return data.getReferralsByProviderSince(providerId, since, toDate, isPaymentsScreen)
    }

    override suspend fun getReferralsByClientPaged(
        clientId: String,
        pageSize: Long,
        lastReferral: Referral?,
        fromDate: Long?,
        toDate: Long?,
        status: String?,
        isPaymentsScreen: Boolean
    ): Pair<List<Referral>, Referral?> {
        return data.getReferralsByClientPaged(clientId, pageSize, lastReferral, fromDate, toDate, status, isPaymentsScreen)
    }

    override suspend fun getReferralsByProviderPaged(
        providerId: String,
        pageSize: Long,
        lastReferral: Referral?,
        fromDate: Long?,
        toDate: Long?,
        status: String?,
        isPaymentsScreen: Boolean
    ): Pair<List<Referral>, Referral?> {
        return data.getReferralsByProviderPaged(providerId, pageSize, lastReferral, fromDate, toDate, status, isPaymentsScreen)
    }
}