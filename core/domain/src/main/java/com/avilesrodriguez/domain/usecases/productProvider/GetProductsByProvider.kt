package com.avilesrodriguez.domain.usecases.productProvider

import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import javax.inject.Inject

class GetProductsByProvider @Inject constructor(
    private val repository: IProductProviderRepository
) {
    suspend operator fun invoke(
        providerId: String,
        pageSize: Long,
        namePrefix: String,
        lastProduct: ProductProvider?
    ) : Pair<List<ProductProvider>, ProductProvider?> {
        return repository.getProductsByProvider(providerId, pageSize, namePrefix, lastProduct)
    }
}