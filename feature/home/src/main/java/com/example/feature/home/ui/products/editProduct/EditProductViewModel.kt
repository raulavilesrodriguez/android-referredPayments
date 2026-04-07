package com.example.feature.home.ui.products.editProduct

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.validationRules.ProductRules
import com.avilesrodriguez.domain.usecases.productProvider.DeactivateProductProvider
import com.avilesrodriguez.domain.usecases.productProvider.GetProductProviderById
import com.avilesrodriguez.domain.usecases.productProvider.UpdateProductProvider
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class EditProductViewModel @Inject constructor(
    private val getProductProviderById: GetProductProviderById,
    private val updateProductProvider: UpdateProductProvider,
    private val deactivateProductProvider: DeactivateProductProvider
) : BaseViewModel() {
    private val _productState = MutableStateFlow(ProductProvider())
    val productState: StateFlow<ProductProvider> = _productState.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val nameProduct
        get() = _productState.value.name

    val description
        get() = _productState.value.description

    val payByReferral
        get() = _productState.value.payByReferral

    fun loadProductInformation(productId: String){
        if(_productState.value.id == productId) return
        _isLoading.value = true
        launchCatching {
            try {
                val product = getProductProviderById(productId)
                _productState.value = product?:ProductProvider()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNameChange(newName: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(ProductRules.MAX_LENGTH_NAME)

        _productState.value = _productState.value.copy(name = filteredName)
        _productState.value = _productState.value.copy(nameLowercase = filteredName.normalizeName())
    }

    fun updateDescription(newDescription: String){
        val filteredDescription = newDescription.take(ProductRules.MAX_LENGTH_PRODUCT_DESCRIPTION)
        _productState.value = _productState.value.copy(description = filteredDescription)
    }

    fun updatePayByReferral(amountUsd: String){
        val filtered = buildString {
            var dotUsed = false
            var decimalsCount = 0
            amountUsd.forEach { char ->
                when {
                    char.isDigit() -> {
                        if (dotUsed) {
                            if (decimalsCount < 2) {
                                append(char)
                                decimalsCount++
                            }
                        } else {
                            if (length == 1 && this[0] == '0') {
                                clear()
                            }
                            append(char)
                        }
                    }
                    char == '.' && !dotUsed -> {
                        if (isEmpty()) append('0')
                        append(char)
                        dotUsed = true
                    }
                }
            }
        }
        _productState.value = _productState.value.copy(payByReferral = filtered)
    }

    private fun normalizeAmount(value: String): String {
        return value.toDoubleOrNull()?.let {
            "%.2f".format(it)
        } ?: ""
    }

    fun onUpdateClick(popUp: () -> Unit){
        val normalizedAmount = normalizeAmount(payByReferral)
        if(nameProduct.isBlank() || description.isBlank() || normalizedAmount.isBlank()){
            return
        }
        _isLoading.value = true
        launchCatching {
            val currentProduct = _productState.value.copy(payByReferral = normalizedAmount)
            val updates = mapOf(
                "name" to currentProduct.name,
                "nameLowercase" to currentProduct.nameLowercase,
                "description" to currentProduct.description,
                "payByReferral" to currentProduct.payByReferral,
                "updatedAt" to System.currentTimeMillis()
            )
            val productId = currentProduct.id
            updateProductProvider(productId, updates)
            popUp()
        }.invokeOnCompletion { _isLoading.value = false }
    }

    fun hideDelete(popUp: () -> Unit){
        _isLoading.value = true
        launchCatching {
            val productId = _productState.value.id
            deactivateProductProvider(productId)
            popUp()
        }.invokeOnCompletion { _isLoading.value = false }
    }
}