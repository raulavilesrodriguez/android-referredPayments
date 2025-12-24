package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IAuthPreferences
import javax.inject.Inject

class IsFirstTime @Inject constructor(
    private val repository: IAuthPreferences
) {
    suspend operator fun invoke(): Boolean = repository.isFirstTime()
}