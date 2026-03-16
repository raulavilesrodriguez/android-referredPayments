package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.FCMTokenDataSource
import com.avilesrodriguez.domain.interfaces.IFCMTokenRepository
import javax.inject.Inject

class FCMTokenRepository @Inject constructor(
    private val dataSource: FCMTokenDataSource
): IFCMTokenRepository {
    override suspend fun getFCMToken(): String {
        return dataSource.getFcmToken() ?: ""
    }
}