package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import jakarta.inject.Inject

class GetUsersProviderByIndustry @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(industry: String) = repository.getUsersProviderByIndustry(industry)
}