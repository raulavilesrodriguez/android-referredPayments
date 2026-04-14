package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.lifecycle.viewModelScope
import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.fcm.ClearFCMToken
import com.avilesrodriguez.domain.usecases.fcm.ClearLocalCache
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.referral.GetReferralsByClientRealTimePagination
import com.avilesrodriguez.domain.usecases.referral.GetReferralsByProviderRealTimePagination
import com.avilesrodriguez.domain.usecases.user.GetUser
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.domain.usecases.account.SignOut
import com.avilesrodriguez.domain.usecases.referral.GetReferrals
import com.avilesrodriguez.presentation.ext.getById
import com.avilesrodriguez.presentation.navigation.ActionOptionsHome
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ReferralsViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val signOut: SignOut,
    private val clearLocalCache: ClearLocalCache,
    private val clearFCMToken: ClearFCMToken,
    private val getReferralsByClientRealTimePagination: GetReferralsByClientRealTimePagination,
    private val getReferralsByProviderRealTimePagination: GetReferralsByProviderRealTimePagination,
    private val getReferrals: GetReferrals
) : BaseViewModel(){
    private val _uiState = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val uiState: StateFlow<List<ReferralWithNames>> = _uiState.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val _referralStatus = MutableStateFlow<ReferralStatus?>(null)
    val referralStatus: StateFlow<ReferralStatus?> = _referralStatus.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isPaginationActive = MutableStateFlow(false)
    val isPaginationActive: StateFlow<Boolean> = _isPaginationActive.asStateFlow()
    private val _allReferralsRealTime = MutableStateFlow<List<Referral>>(emptyList())
    private val _referralsState = MutableStateFlow<List<Referral>>(emptyList())
    val referralsState: StateFlow<List<Referral>> = _referralsState.asStateFlow()
    private var allReferralsLoaded = false
    private var lastReferralViewModel: Referral? = null
    private val pageSize: Long = 3L
    private val pageSizeRealTime: Long = 2L
    private val pageSizeLoadMore: Long = 1L
    private var realTimeJob: Job? = null
    private var paginationJob: Job? = null

    //ConcurrentHashMap guarantees that writings y readings en el caché no corrompan los datos ni lancen una ConcurrentModificationException
    private val nameCache = ConcurrentHashMap<String, String>()

    val referralsStateRealTime: StateFlow<List<Referral>> = combine(
        _allReferralsRealTime,
        _searchText,
        _referralStatus
    ){referrals, query, status ->
        val queryNormalized = query.normalizeName()
        if(queryNormalized.isEmpty() && status == null){
            referrals
        } else {
            referrals.filter { referral ->
                referral.nameLowercase.contains(queryNormalized) && (status == null || referral.status.name == status.name)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
                launch{
                    loadRealData()
                }
            }
        }
    }

    fun updateReferralStatus(newStatus: Int) {
        val status = ReferralStatus.getById(newStatus)
        _referralStatus.value = status
        lastReferralViewModel = null
        allReferralsLoaded = false
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
        lastReferralViewModel = null
        allReferralsLoaded = false
    }

    private  suspend fun transformToReferralsWithOtherName(
        referrals:List<Referral>,
        isProvider: Boolean
    ): List<ReferralWithNames> {
        val uniqueIds = referrals.map { referral ->
            val otherId = if (isProvider) referral.clientId else referral.providerId
            otherId
        }.distinct()
        val missingIds = uniqueIds.filter { !nameCache.containsKey(it) }
        if (missingIds.isNotEmpty()) {
            coroutineScope {
                missingIds.map{id ->
                    async { id to (getUser(id)?.name ?: "") }
                }.awaitAll().forEach { (id, name) ->
                    nameCache[id] = name
                }
            }
        }
        return referrals.map { referral ->
            val otherId = if (isProvider) referral.clientId else referral.providerId
            ReferralWithNames(
                referral = referral,
                otherPartyName = nameCache[otherId] ?: ""
            )
        }
    }

    private fun loadRealData(){
        _isLoading.value = true
        realTimeJob?.cancel()
        realTimeJob = launchCatching {
            val user = _userDataStore.value
            when(user){
                is UserData.Client -> {
                    getReferralsByClientRealTimePagination(currentUserId, pageSizeRealTime, null)
                        .collect { referrals ->
                            _allReferralsRealTime.value = referrals
                            _isLoading.value = false
                        }
                }
                is UserData.Provider -> {
                    getReferralsByProviderRealTimePagination(currentUserId, pageSizeRealTime, null)
                        .collect { referrals ->
                            _allReferralsRealTime.value = referrals
                            _isLoading.value = false
                        }
                }
                else -> {}
            }
        }
    }

    private fun loadInitialReferrals(referralStatus: ReferralStatus?, namePrefix: String){
        _isLoading.value = true
        paginationJob?.cancel()
        paginationJob = launchCatching {
            val user = _userDataStore.value
            try {
                when(user){
                    is UserData.Client -> {
                        val (referrals, lastReferral) = getReferrals(
                            userId = currentUserId,
                            pageSize = pageSize,
                            namePrefix = namePrefix,
                            status = referralStatus?.name,
                            lastReferral = null,
                            isClient = true
                        )
                        _referralsState.value = referrals
                        lastReferralViewModel = lastReferral
                        allReferralsLoaded = referrals.size < pageSize
                    }
                    is UserData.Provider -> {
                        val (referrals, lastReferral) = getReferrals(
                            userId = currentUserId,
                            pageSize = pageSize,
                            namePrefix = namePrefix,
                            status = referralStatus?.name,
                            lastReferral = null,
                            isClient = false
                        )
                        _referralsState.value = referrals
                        lastReferralViewModel = lastReferral
                        allReferralsLoaded = referrals.size < pageSize
                    }
                    else -> return@launchCatching
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreReferrals() {
        if (allReferralsLoaded || paginationJob?.isActive == true || lastReferralViewModel == null) return
        _isLoading.value = true
        paginationJob = launchCatching{
            val user = _userDataStore.value
            val statusSelected = _referralStatus.value?.name
            val namePrefix = _searchText.value
            try {
                when(user){
                    is UserData.Client -> {
                        val (moreReferrals, lastReferral) = getReferrals(
                            userId = currentUserId,
                            pageSize = pageSizeLoadMore,
                            namePrefix = namePrefix,
                            status = statusSelected,
                            lastReferral = lastReferralViewModel,
                            isClient = true
                        )
                        allReferralsLoaded = moreReferrals.size < pageSizeLoadMore
                        if(moreReferrals.isNotEmpty()){
                            val currentReferrals = _referralsState.value.toMutableList()
                            currentReferrals.addAll(moreReferrals)
                            _referralsState.value = currentReferrals.distinctBy { it.id }
                            lastReferralViewModel = lastReferral
                        }
                    }
                    is UserData.Provider -> {
                        val (moreReferrals, lastReferral) = getReferrals(
                            userId = currentUserId,
                            pageSize = pageSizeLoadMore,
                            namePrefix = namePrefix,
                            status = statusSelected,
                            lastReferral = lastReferralViewModel,
                            isClient = false
                        )
                        allReferralsLoaded = moreReferrals.size < pageSizeLoadMore
                        if(moreReferrals.isNotEmpty()){
                            val currentReferrals = _referralsState.value.toMutableList()
                            currentReferrals.addAll(moreReferrals)
                            _referralsState.value = currentReferrals.distinctBy { it.id }
                            lastReferralViewModel = lastReferral
                        }
                    }
                    else -> return@launchCatching
                }
            } finally {
                _isLoading.value = false
            }

        }
    }

    private fun listenToReferralsByClient(status: String?, limit: Long) {
        realTimeJob?.cancel()
        _isLoading.value = true
        realTimeJob = launchCatching {
            getReferralsByClientRealTimePagination(currentUserId, limit, status)
                .collect { referrals ->
                    val enriched = transformToReferralsWithOtherName(referrals, false)
                    _uiState.value = enriched

                    // Si recibimos menos de lo que pedimos, es que ya no hay más en la DB
                    allReferralsLoaded = referrals.size < limit
                    _isLoading.value = false
                }
        }
    }

    private fun listenToReferralsByProvider(status: String?, limit: Long){
        realTimeJob?.cancel()
        _isLoading.value = true
        realTimeJob = launchCatching {
            getReferralsByProviderRealTimePagination(currentUserId, limit, status)
                .collect { referrals ->
                    val enriched = transformToReferralsWithOtherName(referrals, true)
                    _uiState.value = enriched
                    allReferralsLoaded = referrals.size < limit
                    _isLoading.value = false
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

    fun onHome(openScreen: (String) -> Unit) {
        openScreen(NavRoutes.HOME)
    }

    fun onSettings(openScreen: (String) -> Unit) {
        openScreen(NavRoutes.SETTINGS)
    }
}