package com.avilesrodriguez.domain.model.productsProvider

import com.avilesrodriguez.domain.ext.normalizeName

data class ProductProvider(
    val id: String = "",
    val providerId: String = "",
    val name: String = "",
    val nameLowercase: String = name.normalizeName(),
    val description: String? = null,
    val payByReferral: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    // --- DATOS DENORMALIZADOS (Para la UI del Cliente) ---
    val providerName: String = "",
    val providerPhotoUrl: String = "",
    val providerRating: Double = 0.0
)
