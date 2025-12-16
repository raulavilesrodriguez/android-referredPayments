package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IAccountRepository
import javax.inject.Inject

class SendRecoveryEmail @Inject constructor(
    private val repository: IAccountRepository
) {
    suspend operator fun invoke(email: String) {
        repository.sendRecoveryEmail(email)
    }
}