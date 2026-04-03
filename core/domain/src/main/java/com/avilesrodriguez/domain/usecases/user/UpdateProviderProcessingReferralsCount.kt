package com.avilesrodriguez.domain.usecases.user

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import javax.inject.Inject

class UpdateProviderProcessingReferralsCount @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String, increment: Int){
        repository.updateProviderProcessingReferralsCount(uid, increment)
    }
}