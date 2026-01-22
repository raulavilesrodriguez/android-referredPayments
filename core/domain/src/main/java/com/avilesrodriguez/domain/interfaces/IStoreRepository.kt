package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.user.UserData
import kotlinx.coroutines.flow.Flow

interface IStoreRepository {
    suspend fun saveUser(user: UserData)
    suspend fun getUser(uid: String): UserData?
    suspend fun deactivateUser(uid: String)
    suspend fun reactivateUser(uid: String)
    suspend fun secureDeleteAccount(uid: String)
    suspend fun isAuthorizedProvider(email: String): Boolean
    suspend fun getUsersProvider(): Flow<List<UserData>>
    suspend fun getUsersProviderByIndustry(industry: String): Flow<List<UserData>>
    suspend fun searchUsersProvider(namePrefix: String, currentUserId: String): Flow<List<UserData>>
    suspend fun getUsersClient(currentUserId: String): Flow<List<UserData>>
    suspend fun searchUsersClient(namePrefix: String, currentUserId: String): Flow<List<UserData>>
}