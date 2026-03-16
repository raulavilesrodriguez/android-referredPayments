package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.StoreDataSource
import com.avilesrodriguez.domain.interfaces.IInternalTokenRepository
import javax.inject.Inject

class InternalTokenRepository @Inject constructor(
    private val dataSource: StoreDataSource
) : IInternalTokenRepository {
    override suspend fun storeFCMToken(uid: String, token: String) {
        dataSource.storeFCMToken(uid, token)
    }
    override suspend fun clearFCMToken(uid: String) {
        dataSource.clearFCMToken(uid)
    }
}