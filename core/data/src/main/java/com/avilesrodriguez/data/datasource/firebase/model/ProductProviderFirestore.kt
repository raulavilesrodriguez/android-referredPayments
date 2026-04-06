package com.avilesrodriguez.data.datasource.firebase.model

import android.util.Log
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.industries.IndustriesType
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
    val updatedAt: Any? = null,
    val industry: String? = null,
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
        payByReferral = payByReferral.toDouble(),
        isActive = isActive,
        createdAt = Timestamp(Date(createdAt)),
        updatedAt = updatedAt?.let { Timestamp(Date(it)) },
        industry = industry.name,
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
    fun toNullableLong(value: Any?): Long? {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Long -> value
            else -> null
        }
    }

    val domainIndustriesType = try {
        IndustriesType.valueOf(industry?.uppercase() ?: "OTHER")
    } catch (e: Exception) {
        Log.e("ProductProviderFirestore", "Error al convertir el tipo de industria: ${e.message}")
        IndustriesType.OTHER
    }

    fun toDouble(value: Any?): Double {
        return when (value) {
            is Double -> value
            is Long -> value.toDouble()
            else -> 0.0
        }
    }

    return ProductProvider(
        id = id ?: "",
        providerId = providerId ?: "",
        name = name ?: "",
        nameLowercase = nameLowercase ?: (name ?: "").normalizeName(),
        description = description ?: "",
        payByReferral = toDouble(payByReferral).toString(),
        isActive = isActive ?: true,
        createdAt = toLong(createdAt),
        updatedAt = toNullableLong(updatedAt),
        industry = domainIndustriesType,
        providerName = providerName ?: "",
        providerPhotoUrl = providerPhotoUrl ?: "",
        providerRating = providerRating ?: 0.0
    )
}