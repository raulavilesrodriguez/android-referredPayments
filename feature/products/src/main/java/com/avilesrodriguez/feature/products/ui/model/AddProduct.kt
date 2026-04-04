package com.avilesrodriguez.feature.products.ui.model

data class AddProduct(
    val name: String = "",
    val description: String? = null,
    val payByReferral: Double = 0.0
)
