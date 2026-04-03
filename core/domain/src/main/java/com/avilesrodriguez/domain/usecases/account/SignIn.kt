package com.avilesrodriguez.domain.usecases.account

import com.avilesrodriguez.domain.interfaces.IAccountRepository
import javax.inject.Inject

class SignIn @Inject constructor(
    private val repository: IAccountRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.signIn(email, password)
    }
}