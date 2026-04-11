package com.example.feature.home.ui.products.detailProduct

import androidx.lifecycle.viewModelScope
import com.avilesrodriguez.domain.model.businessRules.BusinessRules
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.domain.usecases.productProvider.DeactivateProductProvider
import com.avilesrodriguez.domain.usecases.productProvider.GetProductProviderByIdFlow
import com.avilesrodriguez.domain.usecases.user.GetUser
import com.avilesrodriguez.domain.usecases.user.GetUserFlow
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetailProductViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val getUser: GetUser,
    private val hasUser: HasUser,
    private val getProductProviderByIdFlow: GetProductProviderByIdFlow,
    private val getUserFlow: GetUserFlow
) : BaseViewModel() {
    private val _productId = MutableStateFlow<String?>(null)
    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentUserId get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                val user = getUser(currentUserId)
                _currentUser.value = user
            }
        }
    }

    fun loadProductInformation(productId: String?){
        if (productId == null || _productId.value == productId) return
        _isLoading.value = true
        _productId.value = productId
    }

    val canReferUserClient: StateFlow<Boolean> = _currentUser.map { user ->
        user is UserData.Client
                && user.isActive
                && !user.identityCard.isNullOrBlank()
                && !user.countNumberPay.isNullOrBlank()
                && !user.bankName.isNullOrBlank()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val productState: StateFlow<ProductProvider> = _productId
        .filterNotNull()
        .flatMapLatest { id -> getProductProviderByIdFlow(id) }
        .filterNotNull()
        .onEach { _isLoading.value = false } // Cuando llega el primer dato, quitamos loading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductProvider())

    @OptIn(ExperimentalCoroutinesApi::class)
    val providerUser: StateFlow<UserData?> = productState
        .map { it.providerId }
        .distinctUntilChanged()
        .flatMapLatest { providerId ->
            if (providerId.isEmpty()) flowOf(null)
            else getUserFlow(providerId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isProviderSaturated: StateFlow<Boolean> = providerUser.map { user ->
        val provider = user as? UserData.Provider
        (provider?.processingReferralsCount ?: 0) >= BusinessRules.MAX_PROCESSING_REFERRALS
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onAddReferClick(providerId: String, productId: String, openScreen: (String) -> Unit){
        val route = NavRoutes.NEW_REFERRAL
            .replace("{${NavRoutes.UserArgs.ID}}", providerId)
            .replace("{${NavRoutes.ProductArgs.ID}}", productId)
        openScreen(route)
    }

    fun onEditProductClick(productId: String, openScreen: (String) -> Unit){
        val route = NavRoutes.EDIT_PRODUCT
            .replace("{${NavRoutes.ProductArgs.ID}}", productId)
        openScreen(route)
    }
}