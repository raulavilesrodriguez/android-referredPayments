package com.avilesrodriguez.feature.referrals.ui.referrals

import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.ClearFCMToken
import com.avilesrodriguez.domain.usecases.ClearLocalCache
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClientPaged
import com.avilesrodriguez.domain.usecases.GetReferralsByClientSince
import com.avilesrodriguez.domain.usecases.GetReferralsByProviderPaged
import com.avilesrodriguez.domain.usecases.GetReferralsByProviderSince
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SignOut
import com.avilesrodriguez.presentation.ext.getById
import com.avilesrodriguez.presentation.navigation.ActionOptionsHome
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    private val getReferralsByClientSince: GetReferralsByClientSince,
    private val getReferralsByClientPaged: GetReferralsByClientPaged,
    private val getReferralsByProviderSince: GetReferralsByProviderSince,
    private val getReferralsByProviderPaged: GetReferralsByProviderPaged
) : BaseViewModel(){
    private val _uiState = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val uiState: StateFlow<List<ReferralWithNames>> = _uiState.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val _referralStatus = MutableStateFlow<ReferralStatus?>(null)
    val referralStatus: StateFlow<ReferralStatus?> = _referralStatus.asStateFlow()
    private var allReferralsLoaded = false
    private var lastReferralViewModel: Referral? = null
    private var paginationJob: Job? = null
    private var realTimeJob: Job? = null
    //ConcurrentHashMap guarantees that writings y readings en el caché no corrompan los datos ni lancen una ConcurrentModificationException
    private val nameCache = ConcurrentHashMap<String, String>()

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
                launch{
                    _referralStatus
                        .map { Unit }
                        .collect {
                            lastReferralViewModel = null
                            allReferralsLoaded = false
                            val userData = _userDataStore.value ?: return@collect
                            when(userData) {
                                is UserData.Client -> loadInitialReferralsByClient()
                                is UserData.Provider -> loadInitialReferralsByProvider()
                            }
                        }
                }
            }
        }
    }

    fun updateReferralStatus(newStatus: Int) {
        val status = ReferralStatus.getById(newStatus)
        _referralStatus.value = status
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

    private fun loadInitialReferralsByClient(pageSize: Long=20){
        _isLoading.value = true
        paginationJob?.cancel()
        paginationJob = launchCatching {
            val status = _referralStatus.value?.name
            try {
                val(referrals, lastReferral) = getReferralsByClientPaged(
                    clientId = currentUserId,
                    pageSize = pageSize,
                    lastReferral = null,
                    fromDate = null,
                    toDate = null,
                    status = status,
                    isPaymentsScreen = false
                )
                val result = transformToReferralsWithOtherName(referrals, false)
                _uiState.value = result
                lastReferralViewModel = lastReferral
                allReferralsLoaded = result.size < pageSize
                val newestTime = referrals.firstOrNull()?.createdAt ?: System.currentTimeMillis()
                listenForNewReferralsByClient(currentUserId, newestTime)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForNewReferralsByClient(clientId: String, since: Long) {
        val currentStatus = _referralStatus.value?.name
        realTimeJob?.cancel()
        realTimeJob = launchCatching {
            getReferralsByClientSince(
                clientId = clientId,
                since = since,
                status = currentStatus,
                isPaymentsScreen = false
            )
                .flowOn(Dispatchers.IO)
                .collect { newReferrals ->
                    if(newReferrals.isNotEmpty()){
                        val currentReferrals = _uiState.value
                        val updatedReferrals = (transformToReferralsWithOtherName(newReferrals, false) + currentReferrals).distinctBy { it.referral.id }
                        _uiState.value = updatedReferrals
                    }
                }
        }
    }

    fun loadMoreReferrals(pageSize: Long = 20) {
        val user = _userDataStore.value ?: return
        when(user){
            is UserData.Client -> loadMoreReferralsByClient(pageSize)
            is UserData.Provider -> loadMoreReferralsByProvider(pageSize)
        }
    }

    private fun loadMoreReferralsByClient(pageSize: Long) {
        if (allReferralsLoaded || paginationJob?.isActive == true || lastReferralViewModel == null) return
        _isLoading.value = true
        paginationJob = launchCatching {
            val status = _referralStatus.value?.name
            try {
                val (olderReferrals, lastReferral) = getReferralsByClientPaged(
                    clientId = currentUserId,
                    pageSize = pageSize,
                    lastReferral = lastReferralViewModel,
                    fromDate = null,
                    toDate = null,
                    status = status,
                    isPaymentsScreen = false
                )
                if (olderReferrals.isNotEmpty()) {
                    val currentReferrals = _uiState.value
                    val enriched = transformToReferralsWithOtherName(olderReferrals, false)
                    _uiState.value = (currentReferrals + enriched).distinctBy { it.referral.id }
                }
                lastReferralViewModel = lastReferral
                allReferralsLoaded = olderReferrals.size < pageSize
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadInitialReferralsByProvider(pageSize: Long=20){
        _isLoading.value = true
        paginationJob?.cancel()
        paginationJob = launchCatching {
            val status = _referralStatus.value?.name
            try {
                val(referrals, lastReferral) = getReferralsByProviderPaged(
                    providerId = currentUserId,
                    pageSize = pageSize,
                    lastReferral = null,
                    fromDate = null,
                    toDate = null,
                    status = status,
                    isPaymentsScreen = false
                )
                val result = transformToReferralsWithOtherName(referrals, true)
                _uiState.value = result
                lastReferralViewModel = lastReferral
                allReferralsLoaded = result.size < pageSize
                val newestTime = referrals.firstOrNull()?.createdAt ?: System.currentTimeMillis()
                listenForNewReferralsByProvider(currentUserId, newestTime)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForNewReferralsByProvider(providerId: String, since: Long) {
        val currentStatus = _referralStatus.value?.name
        realTimeJob?.cancel()
        realTimeJob = launchCatching {
            getReferralsByProviderSince(
                providerId = providerId,
                since = since,
                status = currentStatus,
                isPaymentsScreen = false
            )
                .flowOn(Dispatchers.IO)
                .collect { newReferrals ->
                    if(newReferrals.isNotEmpty()){
                        val currentReferrals = _uiState.value
                        val updatedReferrals = (transformToReferralsWithOtherName(newReferrals, true) + currentReferrals).distinctBy { it.referral.id }
                        _uiState.value = updatedReferrals
                    }
                }
        }
    }

    private fun loadMoreReferralsByProvider(pageSize: Long){
        if (allReferralsLoaded || paginationJob?.isActive == true || lastReferralViewModel == null) return
        _isLoading.value = true
        paginationJob = launchCatching {
            val status = _referralStatus.value?.name
            try {
                val (olderReferrals, lastReferral) = getReferralsByProviderPaged(
                    providerId = currentUserId,
                    pageSize = pageSize,
                    lastReferral = lastReferralViewModel,
                    fromDate = null,
                    toDate = null,
                    status = status,
                    isPaymentsScreen = false
                )
                if (olderReferrals.isNotEmpty()) {
                    val currentReferrals = _uiState.value
                    val enriched = transformToReferralsWithOtherName(olderReferrals, true)
                    _uiState.value = (currentReferrals + enriched).distinctBy { it.referral.id }
                }
                lastReferralViewModel = lastReferral
                allReferralsLoaded = olderReferrals.size < pageSize
            } finally {
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