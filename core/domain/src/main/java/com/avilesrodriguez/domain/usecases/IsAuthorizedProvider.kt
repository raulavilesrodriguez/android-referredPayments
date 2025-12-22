package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import javax.inject.Inject

class IsAuthorizedProvider @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return repository.isAuthorizedProvider(email)
    }
}