package com.avilesrodriguez.feature.products.ui.addProduct

import com.avilesrodriguez.domain.model.validationRules.ValidationRules
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.productProvider.SaveProductProvider
import com.avilesrodriguez.domain.usecases.productProvider.UpdateProductProvider
import com.avilesrodriguez.feature.products.ui.model.AddProduct
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val saveProductProvider: SaveProductProvider,
    private val updateProductProvider: UpdateProductProvider
) : BaseViewModel() {
    private val _addProduct = MutableStateFlow(AddProduct())
    val addProduct: StateFlow<AddProduct> = _addProduct.asStateFlow()

    val currentUserId
        get() = currentUserIdUseCase()

    fun onNameChange(newName: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = newName
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(ValidationRules.MAX_LENGTH_NAME)

        _addProduct.value = _addProduct.value.copy(name = filteredName)
    }

}