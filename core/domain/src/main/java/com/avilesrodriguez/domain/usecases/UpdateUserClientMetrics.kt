package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import javax.inject.Inject

class UpdateUserClientMetrics @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String, amountPaid: Double) {
        repository.updateUserClientMetrics(uid, amountPaid)
    }
}