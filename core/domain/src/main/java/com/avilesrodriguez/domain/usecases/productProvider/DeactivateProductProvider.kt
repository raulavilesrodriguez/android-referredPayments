package com.avilesrodriguez.domain.usecases.productProvider

import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import javax.inject.Inject

class DeactivateProductProvider @Inject constructor(
    private val repository: IProductProviderRepository
) {
    suspend operator fun invoke(productProviderId: String) {
        repository.deactivateProductProvider(productProviderId)
    }
}