package com.avilesrodriguez.domain.usecases.account

import com.avilesrodriguez.domain.interfaces.IAccountRepository
import javax.inject.Inject

class DeleteAccount @Inject constructor(
    private val repository: IAccountRepository
) {
    suspend operator fun invoke() = repository.deleteAccount()
}