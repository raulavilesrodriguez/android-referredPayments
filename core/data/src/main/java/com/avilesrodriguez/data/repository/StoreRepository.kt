package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.StoreDataSource
import com.avilesrodriguez.domain.interfaces.IStoreRepository
import com.avilesrodriguez.domain.model.user.UserData
import javax.inject.Inject

class StoreRepository @Inject constructor(
    private val data: StoreDataSource
): IStoreRepository {
    override suspend fun saveUser(user: UserData) = data.saveUser(user)

    override suspend fun getUser(uid: String): UserData? {
        return data.getUser(uid)
    }

    override suspend fun deactivateUser(uid: String) = data.deactivateUser(uid)

    override suspend fun reactivateUser(uid: String) = data.reactivateUser(uid)

    override suspend fun secureDeleteAccount(uid: String) = data.secureDeleteAccount(uid)

    override suspend fun isAuthorizedProvider(email: String): Boolean {
        return data.isAuthorizedProvider(email)
    }
}