package com.example.feature.home.ui

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByClientByProvider
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SearchUsersClient
import com.avilesrodriguez.domain.usecases.SearchUsersProvider
import com.avilesrodriguez.domain.usecases.SignOut
import com.avilesrodriguez.presentation.industries.getById
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import com.example.feature.home.models.UserAndReferralMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val signOut: SignOut,
    private val searchUsersProvider: SearchUsersProvider,
    private val searchUsersClient: SearchUsersClient,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val getReferralsByClientByProvider: GetReferralsByClientByProvider,
    private val getReferralsByClient: GetReferralsByClient
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

    private var searchJob: Job? = null
    private var referralsJob: Job? = null
    private var userMetricsJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    init{
        launchCatching {
            if(hasUser()){
                val user = getUser(currentUserId)
                _userDataStore.value = user
                when(user){
                    is UserData.Provider ->{
                        loadReferralsByProvider()
                        observeUsersMetrics()
                    }
                    is UserData.Client -> {
                        loadReferralsByClient()
                    }
                    else -> {}
                }
            }
            combine(_searchText, _selectedIndustry){ text, industry ->
                Pair(text, industry)
            }
                .debounce(300)
                .distinctUntilChanged()
                .collect { (query, industry) ->
                    searchJob?.cancel() // cancel previous search
                    loadData(query.normalizeName(), industry)
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

    private fun loadData(query: String, industry: IndustriesType?){
        _isLoading.value = true
        searchJob = launchCatching {
            val user = _userDataStore.value
            when(user){
                is UserData.Client -> {
                    searchUsersProvider(query, industry?.name)
                        .collect { users ->
                            _users.value = users
                            _isLoading.value = false
                        }
                }
                is UserData.Provider -> {
                    searchUsersClient(query, currentUserId)
                        .collect { users ->
                            _users.value = users
                            _isLoading.value = false
                        }
                }
                else -> {emptyList<UserData>()}
            }
        }
    }

    fun onActionClick(openScreen: (String) -> Unit, restartApp: (String) -> Unit, action: Int){
        when(ActionOptionsHome.getById(action)){
            ActionOptionsHome.POLICIES -> openScreen(NavRoutes.POLICIES)
            ActionOptionsHome.SIGN_OUT -> launchCatching {
                signOut()
                restartApp(NavRoutes.SPLASH)
            }
        }
    }

    fun editUser(openScreen: (String) -> Unit){
        openScreen(NavRoutes.EDIT_USER)
    }

    fun navigationUserDetails(uid:String, openScreen: (String) -> Unit){
        val route = NavRoutes.USER_DETAIL.replace("{${NavRoutes.UserArgs.ID}}", uid)
        openScreen(route)
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

}