package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IAccountRepository
import javax.inject.Inject

class CurrentUserId @Inject constructor(
    private val repository: IAccountRepository
) {
    operator fun invoke(): String = repository.currentUserId
}