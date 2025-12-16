package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.AccountDataSource
import com.avilesrodriguez.domain.interfaces.IAccountRepository
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val data: AccountDataSource
): IAccountRepository {
    override val currentUserId: String
        get() = data.currentUserId

    override val hasUser: Boolean
        get() = data.hasUser

    override suspend fun signUp(email: String, password: String) {
        data.signUp(email, password)
    }

    override suspend fun signIn(email: String, password: String) {
        data.signIn(email, password)
    }

    override suspend fun sendRecoveryEmail(email: String) {
        data.sendRecoveryEmail(email)
    }

    override suspend fun signOut() {
        data.signOut()
    }

    override suspend fun deleteAccount() {
        data.deleteAccount()
    }
}