package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import javax.inject.Inject

class UpdateUser @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String, updates: Map<String, Any>){
        repository.updateUser(uid, updates)
    }
}