package com.avilesrodriguez.domain.usecases.productProvider

import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import javax.inject.Inject

class UpdateProductProvider @Inject constructor(
    private val repository: IProductProviderRepository
) {
    suspend operator fun invoke(id: String, updates: Map<String, Any>) {
        repository.updateProductProvider(id, updates)
    }
}