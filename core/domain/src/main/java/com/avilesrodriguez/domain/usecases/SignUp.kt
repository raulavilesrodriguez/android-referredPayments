package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IAccountRepository
import javax.inject.Inject

class SignUp @Inject constructor(
    private val repository: IAccountRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.signUp(email, password)
    }
}