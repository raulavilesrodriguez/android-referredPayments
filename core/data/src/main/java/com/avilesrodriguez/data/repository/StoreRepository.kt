package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.StoreDataSource
import com.avilesrodriguez.domain.interfaces.IStoreRepository
import com.avilesrodriguez.domain.model.user.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StoreRepository @Inject constructor(
    private val data: StoreDataSource
): IStoreRepository {
    override suspend fun saveUser(user: UserData) = data.saveUser(user)

    override suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        data.updateUser(uid, updates)
    }

    override suspend fun updateUserClientMetrics(uid: String, amountPaid: Double) {
        data.updateUserClientMetrics(uid, amountPaid)
    }

    override suspend fun updateUserProviderMetrics(
        uid: String,
        moneyPaid: Double,
        referralsConversion: String
    ) {
        data.updateUserProviderMetrics(uid, moneyPaid, referralsConversion)
    }

    override suspend fun getUser(uid: String): UserData? {
        return data.getUser(uid)
    }

    override suspend fun deactivateUser(uid: String) = data.deactivateUser(uid)

    override suspend fun reactivateUser(uid: String) = data.reactivateUser(uid)

    override suspend fun secureDeleteAccount(uid: String) = data.secureDeleteAccount(uid)

    override suspend fun isAuthorizedProvider(email: String): Boolean {
        return data.isAuthorizedProvider(email)
    }

    override suspend fun getUsersProvider(): Flow<List<UserData>> {
        return data.getUsersProvider()
    }

    override suspend fun getUsersProviderByIndustry(industry: String): Flow<List<UserData>> {
        return data.getUsersProviderByIndustry(industry)
    }

    override suspend fun searchUsersProvider(
        namePrefix: String,
        industry: String?
    ): Flow<List<UserData>> {
        return data.searchUsersProvider(namePrefix, industry)
    }

    override suspend fun getUsersClient(currentUserId: String): Flow<List<UserData>> =
        data.getUsersClient(currentUserId)


    override suspend fun searchUsersClient(
        namePrefix: String,
        currentUserId: String
    ): Flow<List<UserData>> {
        return data.searchUsersClient(namePrefix, currentUserId)
    }

    override suspend fun getUserFlow(uid: String): Flow<UserData?> {
        return data.getUserFlow(uid)
    }
}