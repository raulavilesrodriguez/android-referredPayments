package com.avilesrodriguez.domain.usecases.productProvider

import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsByProviderRealTime @Inject constructor(
    private val repository: IProductProviderRepository
) {
    operator fun invoke(providerId: String, limit: Long) : Flow<List<ProductProvider>> =
        repository.getProductsByProviderRealTime(providerId, limit)
}