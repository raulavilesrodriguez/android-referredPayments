package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IAccountRepository
import javax.inject.Inject

class HasUser @Inject constructor(
    private val repository: IAccountRepository
) {
    operator fun invoke(): Boolean = repository.hasUser
}