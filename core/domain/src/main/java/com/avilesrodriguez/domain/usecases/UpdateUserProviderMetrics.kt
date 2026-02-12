package com.avilesrodriguez.domain.usecases

import com.avilesrodriguez.domain.interfaces.IStoreRepository
import javax.inject.Inject

class UpdateUserProviderMetrics @Inject constructor(
    private val repository: IStoreRepository
) {
    suspend operator fun invoke(uid: String, moneyPaid: Double, referralsConversion: String){
        repository.updateUserProviderMetrics(uid, moneyPaid, referralsConversion)
    }
}