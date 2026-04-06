package com.example.feature.home.ui.products.model

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider

data class AddProduct(
    val name: String = "",
    val description: String = "",
    val payByReferral: String = "",
)

fun AddProduct.toProductProvider(
    providerId: String,
    createdAt: Long,
    updatedAt: Long? = null,
    providerName: String = "",
    providerPhotoUrl: String = "",
    providerRating: Double = 0.0,
    industry: IndustriesType = IndustriesType.OTHER
): ProductProvider {
    return ProductProvider(
        providerId = providerId,
        name = name,
        nameLowercase = name.normalizeName(),
        description = description,
        payByReferral = payByReferral,
        industry = industry,
        createdAt = createdAt,
        updatedAt = updatedAt,
        providerName = providerName,
        providerPhotoUrl = providerPhotoUrl,
        providerRating = providerRating
    )
}