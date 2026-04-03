package com.avilesrodriguez.domain.interfaces

import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface IProductProviderRepository {
    suspend fun saveProductProvider(productProvider: ProductProvider)
    suspend fun updateProductProvider(id: String, updates: Map<String, Any>)
    suspend fun deactivateProductProvider(productProviderId: String)
    suspend fun getProductProviderById(productProviderId: String): ProductProvider?
    fun getProductProviderByIdFlow(id: String): Flow<ProductProvider?>
    fun getProductsRealTime(limit: Long) : Flow<List<ProductProvider>>
    fun getProductsByProviderRealTime(providerId: String, limit: Long) : Flow<List<ProductProvider>>
    suspend fun getAllProducts(pageSize: Long, industry: String? = null, namePrefix: String, lastProduct: ProductProvider? = null) : Pair<List<ProductProvider>, ProductProvider?>
    suspend fun getProductsByProvider(providerId: String, pageSize: Long, namePrefix: String, lastProduct: ProductProvider? = null) : Pair<List<ProductProvider>, ProductProvider?>
}