package com.avilesrodriguez.domain.usecases.productProvider

import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductProviderByIdFlow @Inject constructor(
    private val repository: IProductProviderRepository
) {
    operator fun invoke(id: String) : Flow<ProductProvider?> = repository.getProductProviderByIdFlow(id)
}