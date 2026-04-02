package com.avilesrodriguez.data.datasource.firebase.model

import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.google.firebase.Timestamp
import java.util.Date

data class ProductProviderFirestore(
    val id: String? = null,
    val providerId: String? = null,
    val name: String? = null,
    val nameLowercase: String? = null,
    val description: String? = null,
    val payByReferral: Double? = null,
    val isActive: Boolean? = null,
    val createdAt: Any? = null,
    // --- DATOS DENORMALIZADOS (Para la UI del Cliente) ---
    val providerName: String? = null,
    val providerPhotoUrl: String? = null,
    val providerRating: Double? = null
)

fun ProductProvider.toProductProviderFirestore(): ProductProviderFirestore {
    return ProductProviderFirestore(
        id = id,
        providerId = providerId,
        name = name,
        nameLowercase = nameLowercase,
        description = description,
        payByReferral = payByReferral,
        isActive = isActive,
        createdAt = Timestamp(Date(createdAt)),
        providerName = providerName,
        providerPhotoUrl = providerPhotoUrl,
        providerRating = providerRating
    )
}

fun ProductProviderFirestore.toProductProviderDomain(): ProductProvider {
    fun toLong(value: Any?): Long {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Long -> value
            else -> System.currentTimeMillis()
        }
    }

    return ProductProvider(
        id = id ?: "",
        providerId = providerId ?: "",
        name = name ?: "",
        nameLowercase = nameLowercase ?: "",
        description = description,
        payByReferral = payByReferral ?: 0.0,
        isActive = isActive ?: true,
        createdAt = toLong(createdAt),
        providerName = providerName ?: "",
        providerPhotoUrl = providerPhotoUrl ?: "",
        providerRating = providerRating ?: 0.0
    )
}