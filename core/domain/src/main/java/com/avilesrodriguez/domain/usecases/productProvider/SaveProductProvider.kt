package com.avilesrodriguez.domain.usecases.productProvider

import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import javax.inject.Inject

class SaveProductProvider @Inject constructor(
    private val repository: IProductProviderRepository
) {
    suspend operator fun invoke(productProvider: ProductProvider) {
        repository.saveProductProvider(productProvider)
    }
}