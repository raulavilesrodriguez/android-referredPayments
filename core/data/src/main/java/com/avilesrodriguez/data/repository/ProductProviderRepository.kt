package com.avilesrodriguez.data.repository

import com.avilesrodriguez.data.datasource.firebase.ProductProviderDataSource
import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductProviderRepository @Inject constructor(
    private val data: ProductProviderDataSource
) : IProductProviderRepository {
    override suspend fun saveProductProvider(productProvider: ProductProvider) {
        data.saveProductProvider(productProvider)
    }

    override suspend fun updateProductProvider(id: String, updates: Map<String, Any>) {
        data.updateProductProvider(id, updates)
    }

    override suspend fun deactivateProductProvider(productProviderId: String) {
        data.deactivateProductProvider(productProviderId)
    }

    override suspend fun getProductProviderById(productProviderId: String): ProductProvider? {
        return data.getProductProviderById(productProviderId)
    }

    override fun getProductProviderByIdFlow(id: String): Flow<ProductProvider?> {
        return data.getProductProviderByIdFlow(id)
    }

    override fun getProductsRealTime(limit: Long): Flow<List<ProductProvider>> {
        return data.getProductsRealTime(limit)
    }

    override fun getProductsByProviderRealTime(
        providerId: String,
        limit: Long
    ): Flow<List<ProductProvider>> {
        return data.getProductsByProviderRealTime(providerId, limit)
    }

    override suspend fun getAllProducts(
        pageSize: Long,
        industry: String?,
        namePrefix: String,
        lastProduct: ProductProvider?
    ): Pair<List<ProductProvider>, ProductProvider?> {
        return data.getAllProducts(pageSize, industry, namePrefix, lastProduct)
    }

    override suspend fun getProductsByProvider(
        providerId: String,
        pageSize: Long,
        namePrefix: String,
        lastProduct: ProductProvider?
    ): Pair<List<ProductProvider>, ProductProvider?> {
        return data.getProductsByProvider(providerId, pageSize, namePrefix, lastProduct)
    }
}