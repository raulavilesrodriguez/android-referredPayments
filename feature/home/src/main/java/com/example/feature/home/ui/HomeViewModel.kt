package com.example.feature.home.ui

import androidx.lifecycle.viewModelScope
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.fcm.ClearFCMToken
import com.avilesrodriguez.domain.usecases.fcm.ClearLocalCache
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.fcm.GetAndStoreFCMToken
import com.avilesrodriguez.domain.usecases.referral.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.referral.GetReferralsByClientByProvider
import com.avilesrodriguez.domain.usecases.referral.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.user.GetUserFlow
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.domain.usecases.account.SignOut
import com.avilesrodriguez.domain.usecases.productProvider.DeactivateProductProvider
import com.avilesrodriguez.domain.usecases.productProvider.GetAllProducts
import com.avilesrodriguez.domain.usecases.productProvider.GetProductsByProvider
import com.avilesrodriguez.domain.usecases.productProvider.GetProductsByProviderRealTime
import com.avilesrodriguez.domain.usecases.productProvider.GetProductsRealTime
import com.avilesrodriguez.presentation.industries.getById
import com.avilesrodriguez.presentation.navigation.ActionOptionsHome
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import com.example.feature.home.models.UserAndReferralMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val signOut: SignOut,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val getReferralsByClientByProvider: GetReferralsByClientByProvider,
    private val getReferralsByClient: GetReferralsByClient,
    private val getUserFlow: GetUserFlow,
    private val clearLocalCache: ClearLocalCache,
    private val clearFCMToken: ClearFCMToken,
    private val getAndStoreFCMToken: GetAndStoreFCMToken,
    private val getProductsRealTime: GetProductsRealTime,
    private val getAllProducts: GetAllProducts,
    private val getProductsByProviderRealTime: GetProductsByProviderRealTime,
    private val getProductsByProvider: GetProductsByProvider,
    private val deactivateProductProvider: DeactivateProductProvider
) : BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedIndustry = MutableStateFlow<IndustriesType?>(null)
    val selectedIndustry: StateFlow<IndustriesType?> = _selectedIndustry.asStateFlow()

    private val _uiStateReferralsMetrics = MutableStateFlow(ReferralMetrics())
    val uiStateReferralsMetrics: StateFlow<ReferralMetrics> = _uiStateReferralsMetrics.asStateFlow()

    private val _usersAndMetrics = MutableStateFlow<List<UserAndReferralMetrics>>(emptyList())
    val usersAndMetrics: StateFlow<List<UserAndReferralMetrics>> = _usersAndMetrics.asStateFlow()
    private val _referralsProvider = MutableStateFlow<List<Referral>>(emptyList())
    private val _allProductsRealTime = MutableStateFlow<List<ProductProvider>>(emptyList())
    private val _isPaginationActive = MutableStateFlow(false)
    val isPaginationActive: StateFlow<Boolean> = _isPaginationActive.asStateFlow()

    val productsStateRealTime: StateFlow<List<ProductProvider>> = combine(
        _allProductsRealTime,
        _searchText,
        _selectedIndustry
    ) { products, query, industry ->
        val queryNormalized = query.normalizeName()
        if (queryNormalized.isEmpty() && industry == null) {
            products
        } else {
            products.filter { product ->
                product.nameLowercase.contains(queryNormalized) && (industry == null || product.industry == industry)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val showViewMoreButton: StateFlow<Boolean> = combine(
        _allProductsRealTime,
        _isPaginationActive
    ) { products, active ->
        products.size >= pageSize && !active
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _productsState = MutableStateFlow<List<ProductProvider>>(emptyList())
    val productsState: StateFlow<List<ProductProvider>> = _productsState.asStateFlow()
    private var lastProductViewModel: ProductProvider? = null
    private var allProductsLoaded = false
    private val pageSize: Long = 3L
    private val pageSizeLoadMore: Long = 3L
    private var referralsJob: Job? = null
    private var userMetricsJob: Job? = null
    private var paginationJob: Job? = null
    private var realTimeJob: Job? = null
    val processingCountReferralsProvider: StateFlow<Int> = _referralsProvider.map {referrals ->
        val activeStatuses = listOf(ReferralStatus.PROCESSING, ReferralStatus.PENDING)
        referrals.count { it.status in activeStatuses }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val referralsConversion: StateFlow<Double> = combine(
        _userDataStore,
        _referralsProvider
    ) { user, referrals ->
        val provider = user as? UserData.Provider
        if (provider != null && referrals.isNotEmpty()) {
            provider.totalPayouts.toDouble() / referrals.size
        } else {
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)  //5000 retraso de 5seg

    val canReferUserClient: StateFlow<Boolean> = _userDataStore.map { user ->
        user is UserData.Client
                && user.isActive
                && !user.identityCard.isNullOrBlank()
                && !user.countNumberPay.isNullOrBlank()
                && !user.bankName.isNullOrBlank()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if (hasUser()) {
                launch {
                    getUserFlow(currentUserId).collect {
                        _userDataStore.value = it
                    }
                }
                launch {
                    _userDataStore.filterNotNull().first().let { user ->
                        launchCatching(snackbar = false) {
                            getAndStoreFCMToken(user.uid)
                        }
                        when (user) {
                            is UserData.Provider -> {
                                loadReferralsByProvider()
                                observeUsersMetrics()
                            }

                            is UserData.Client -> {
                                loadReferralsByClient()
                            }
                        }
                        loadRealData()
                    }
                }
            }
            combine(_searchText, _selectedIndustry) { text, industry ->
                Pair(text, industry)
            }
                .debounce(300)
                .distinctUntilChanged()
                .collect { (query, industry) ->
                    loadInitialProducts(industry = industry, namePrefix = query)
                }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
    }

    fun onIndustryChange(industry: Int){
        val filteredNameIndustry = IndustriesType.getById(industry)
        _selectedIndustry.value = filteredNameIndustry
    }

    private fun loadRealData() {
        _isLoading.value = true
        realTimeJob?.cancel()
        realTimeJob = launchCatching {
            val user = _userDataStore.value
            when (user) {
                is UserData.Client -> {
                    getProductsRealTime(limit = pageSize)
                        .collect { products ->
                            _allProductsRealTime.value = products
                            _isLoading.value = false
                        }
                }

                is UserData.Provider -> {
                    getProductsByProviderRealTime(providerId = currentUserId, limit = pageSize)
                        .collect { products ->
                            _allProductsRealTime.value = products
                            _isLoading.value = false
                        }
                }

                else -> {}
            }
        }
    }


    fun onViewMoreProducts() {
        _isPaginationActive.value = true
    }

    private fun loadInitialProducts(industry: IndustriesType?, namePrefix: String){
        _isLoading.value = true
        paginationJob?.cancel()
        paginationJob = launchCatching {
            val user = _userDataStore.value
            try {
                when(user){
                    is UserData.Client ->{
                        val (products, lastProduct) = getAllProducts(
                            pageSize = pageSize,
                            industry = industry?.name,
                            namePrefix = namePrefix,
                            lastProduct = null
                        )
                        _productsState.value = products
                        lastProductViewModel = lastProduct
                        allProductsLoaded = products.size < pageSize
                    }
                    is UserData.Provider -> {
                        val (products, lastProduct) = getProductsByProvider(
                            providerId = currentUserId,
                            pageSize = pageSize,
                            namePrefix = namePrefix,
                            lastProduct = null
                        )
                        _productsState.value = products
                        lastProductViewModel = lastProduct
                        allProductsLoaded = products.size < pageSize
                    }
                    else -> return@launchCatching
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreProducts(){
        if(allProductsLoaded || paginationJob?.isActive == true || lastProductViewModel == null) return
        _isLoading.value =true
        paginationJob = launchCatching {
            val user = _userDataStore.value
            val industrySelected = _selectedIndustry.value
            val namePrefix = _searchText.value
            when(user){
                is UserData.Client -> {
                    val (moreProducts, lastProduct) = getAllProducts(
                        pageSize = pageSizeLoadMore,
                        industry = industrySelected?.name,
                        namePrefix = namePrefix,
                        lastProduct = lastProductViewModel
                    )
                    if(moreProducts.isNotEmpty()){
                        val currentProducts = _productsState.value.toMutableList()
                        currentProducts.addAll(moreProducts)
                        val currentProductsFiltered = currentProducts.distinctBy { it.id }
                        _productsState.value = currentProductsFiltered
                        lastProductViewModel = lastProduct
                    }
                }
                is UserData.Provider -> {
                    val (moreProducts, lastProduct) = getProductsByProvider(
                        providerId = currentUserId,
                        pageSize = pageSize,
                        namePrefix = namePrefix,
                        lastProduct = lastProductViewModel
                    )
                    if(moreProducts.isNotEmpty()){
                        val currentProducts = _productsState.value.toMutableList()
                        currentProducts.addAll(moreProducts)
                        val currentProductsFiltered = currentProducts.distinctBy { it.id }
                        _productsState.value = currentProductsFiltered
                        lastProductViewModel = lastProduct
                    }
                }
                else -> return@launchCatching
            }
        }
    }

    fun onActionClick(openScreen: (String) -> Unit, restartApp: (String) -> Unit, action: Int){
        when(ActionOptionsHome.getById(action)){
            ActionOptionsHome.POLICIES -> openScreen(NavRoutes.POLICIES)
            ActionOptionsHome.SIGN_OUT -> launchCatching {
                val userId = currentUserId
                clearFCMToken(userId)
                clearLocalCache()
                signOut()
                restartApp(NavRoutes.SPLASH)
            }
        }
    }

    private fun updateMetrics(referrals: List<Referral>) {
        _uiStateReferralsMetrics.value = ReferralMetrics(
            totalReferrals = referrals.size,
            pendingReferrals = referrals.count { it.status == ReferralStatus.PENDING },
            processingReferrals = referrals.count { it.status == ReferralStatus.PROCESSING },
            rejectedReferrals = referrals.count { it.status == ReferralStatus.REJECTED },
            paidReferrals = referrals.count { it.status == ReferralStatus.PAID }
        )
    }

    private fun loadReferralsByProvider(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByProvider(currentUserId)
                .collect { referrals ->
                    updateMetrics(referrals)
                    _referralsProvider.value = referrals
                    _isLoading.value = false
                }
        }
    }

    private fun loadReferralsByClient(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByClient(currentUserId)
                .collect { referrals ->
                    updateMetrics(referrals)
                    _isLoading.value = false
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUsersMetrics(){
        _isLoading.value = true
        userMetricsJob?.cancel()
        userMetricsJob = launchCatching {
            _users.flatMapLatest { userList ->
                if (userList.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows = userList.map { user ->
                        getReferralsByClientByProvider(user.uid, currentUserId).map { referrals ->
                            UserAndReferralMetrics(
                                user = user,
                                referralMetrics = ReferralMetrics(
                                    totalReferrals = referrals.size,
                                    pendingReferrals = referrals.count { it.status == ReferralStatus.PENDING },
                                    processingReferrals = referrals.count { it.status == ReferralStatus.PROCESSING },
                                    rejectedReferrals = referrals.count { it.status == ReferralStatus.REJECTED },
                                    paidReferrals = referrals.count { it.status == ReferralStatus.PAID }
                                )
                            )
                        }
                    }
                    combine(flows) { array -> array.toList() }
                }
            }.collect { combinedList ->
                _usersAndMetrics.value = combinedList
                _isLoading.value = false 
            }
        }
    }

    fun onReferrals(openScreen: (String) -> Unit){
        openScreen(NavRoutes.REFERRALS)
    }

    fun onSettings(openScreen: (String) -> Unit){
        openScreen(NavRoutes.SETTINGS)
    }

    fun hideDelete(productId:String){
        _isLoading.value = true
        _isPaginationActive.value = false
        _selectedIndustry.value = null
        _searchText.value = ""
        launchCatching {
            deactivateProductProvider(productId)
        }.invokeOnCompletion { _isLoading.value = false }
    }

}