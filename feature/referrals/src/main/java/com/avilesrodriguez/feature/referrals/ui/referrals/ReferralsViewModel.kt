package com.avilesrodriguez.feature.referrals.ui.referrals

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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val getReferralsByProviderRealTimePagination: GetReferralsByProviderRealTimePagination
) : BaseViewModel(){
    private val _uiState = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val uiState: StateFlow<List<ReferralWithNames>> = _uiState.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val _referralStatus = MutableStateFlow<ReferralStatus?>(null)
    val referralStatus: StateFlow<ReferralStatus?> = _referralStatus.asStateFlow()
    private val _pageSize = MutableStateFlow(20L)
    private var allReferralsLoaded = false
    private var lastReferralViewModel: Referral? = null
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
                    combine(_referralStatus, _pageSize) { status, size -> status to size }
                        .collect { (status, size) ->
                            lastReferralViewModel = null
                            allReferralsLoaded = false
                            val userData = _userDataStore.value ?: return@collect
                            when(userData) {
                                is UserData.Client -> listenToReferralsByClient(status = status?.name, limit = size)
                                is UserData.Provider -> listenToReferralsByProvider(status = status?.name, limit = size)
                            }
                        }
                }
            }
        }
    }

    fun updateReferralStatus(newStatus: Int) {
        val status = ReferralStatus.getById(newStatus)
        _referralStatus.value = status
        _pageSize.value = 20L
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

    fun loadMoreReferrals() {
        if (allReferralsLoaded || _isLoading.value) return
        _pageSize.value +=20
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