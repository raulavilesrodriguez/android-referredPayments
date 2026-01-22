package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import com.avilesrodriguez.domain.model.user.UserData
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetUsersProvider @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(): Flow<List<UserData>> = repository.getUsersProvider()
}