package com.avilesrodriguez.domain.model.productsProvider

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.industries.IndustriesType

data class ProductProvider(
    val id: String = "",
    val providerId: String = "",
    val name: String = "",
    val nameLowercase: String = name.normalizeName(),
    val description: String? = null,
    val payByReferral: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val industry: IndustriesType = IndustriesType.OTHER,
    // --- DATOS DENORMALIZADOS (Para la UI del Cliente) ---
    val providerName: String = "",
    val providerPhotoUrl: String = "",
    val providerRating: Double = 0.0
)
