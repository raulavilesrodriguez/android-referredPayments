package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import com.avilesrodriguez.domain.model.user.UserData
import javax.inject.Inject

class GetUser @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String): UserData? {
        return repository.getUser(uid)
    }
}