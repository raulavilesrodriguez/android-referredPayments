package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import com.avilesrodriguez.domain.model.user.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserFlow @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String):Flow<UserData?> = repository.getUserFlow(uid)
}