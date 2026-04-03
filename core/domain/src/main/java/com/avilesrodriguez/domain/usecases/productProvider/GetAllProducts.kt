package com.avilesrodriguez.domain.usecases.productProvider

import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import javax.inject.Inject

class GetAllProducts @Inject constructor(
    private val repository: IProductProviderRepository
) {
    suspend operator fun invoke(
        pageSize: Long,
        industry: String?,
        namePrefix: String,
        lastProduct: ProductProvider?
    ) : Pair<List<ProductProvider>, ProductProvider?> {
        return repository.getAllProducts(pageSize, industry, namePrefix, lastProduct)
    }
}