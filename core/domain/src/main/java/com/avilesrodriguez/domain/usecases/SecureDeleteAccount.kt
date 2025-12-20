package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import javax.inject.Inject

class SecureDeleteAccount @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String) = repository.secureDeleteAccount(uid)
}