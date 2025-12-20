package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.user.UserData

interface IStoreRepository {
    suspend fun saveUser(user: UserData)
    suspend fun getUser(uid: String): UserData?
    suspend fun deactivateUser(uid: String)
    suspend fun reactivateUser(uid: String)
    suspend fun secureDeleteAccount(uid: String)
}