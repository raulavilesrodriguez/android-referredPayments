package com.example.feature.home.ui.products.addProduct

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.validationRules.ProductRules
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.domain.usecases.productProvider.SaveProductProvider
import com.avilesrodriguez.domain.usecases.user.GetUser
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import com.example.feature.home.ui.products.model.AddProduct
import com.example.feature.home.ui.products.model.toProductProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val getUser: GetUser,
    private val hasUser: HasUser,
    private val saveProductProvider: SaveProductProvider,
) : BaseViewModel() {
    private val _addProduct = MutableStateFlow(AddProduct())
    val addProduct: StateFlow<AddProduct> = _addProduct.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _providerUser = MutableStateFlow<UserData?>(null)
    val providerUser: StateFlow<UserData?> = _providerUser.asStateFlow()

    val currentUserId
        get() = currentUserIdUseCase()

    val nameProduct
        get() = _addProduct.value.name

    val description
        get() = _addProduct.value.description

    val payByReferral
        get() = _addProduct.value.payByReferral

    init {
        launchCatching {
            if(hasUser()){
                val user = getUser(currentUserId)
                _providerUser.value = user
            }
        }
    }

    fun onNameChange(newName: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(ProductRules.MAX_LENGTH_NAME)

        _addProduct.value = _addProduct.value.copy(name = filteredName)
    }

    fun updateDescription(newDescription: String){
        val filteredDescription = newDescription.take(ProductRules.MAX_LENGTH_PRODUCT_DESCRIPTION)
        _addProduct.value = _addProduct.value.copy(description = filteredDescription)
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
        _addProduct.value = _addProduct.value.copy(payByReferral = filtered)
    }

    private fun normalizeAmount(value: String): String {
        return value.toDoubleOrNull()?.let {
            "%.2f".format(it)
        } ?: ""
    }

    fun onSaveClick(popUp: () -> Unit){
        val normalizedAmount = normalizeAmount(payByReferral)
        if(nameProduct.isBlank() || description.isBlank() || normalizedAmount.isBlank()){
            return
        }

        _isLoading.value = true
        launchCatching {
            val user = _providerUser.value ?: return@launchCatching
            val provider = user as UserData.Provider
            val currentState = _addProduct.value.copy(payByReferral = normalizedAmount)
            val product = currentState.toProductProvider(
                providerId = currentUserId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                providerName = provider.name?:"",
                providerPhotoUrl = provider.photoUrl,
                providerRating = provider.paymentRating,
                industry = provider.industry,
                isActive = true
            )
            saveProductProvider(product)
            _addProduct.value = AddProduct()
            popUp()
        }.invokeOnCompletion { _isLoading.value = false }
    }

}