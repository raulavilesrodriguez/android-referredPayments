package com.avilesrodriguez.feature.referrals.ui.referrals

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.ClearFCMToken
import com.avilesrodriguez.domain.usecases.ClearLocalCache
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByClientPaged
import com.avilesrodriguez.domain.usecases.GetReferralsByClientSince
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetReferralsByProviderPaged
import com.avilesrodriguez.domain.usecases.GetReferralsByProviderSince
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SignOut
import com.avilesrodriguez.feature.referrals.ui.referral.ReferralStatus
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(FlowPreview::class)
@HiltViewModel
class ReferralsViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralsByClient: GetReferralsByClient,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val signOut: SignOut,
    private val clearLocalCache: ClearLocalCache,
    private val clearFCMToken: ClearFCMToken,
    private val getReferralsByClientSince: GetReferralsByClientSince,
    private val getReferralsByClientPaged: GetReferralsByClientPaged,
    private val getReferralsByProviderSince: GetReferralsByProviderSince,
    private val getReferralsByProviderPaged: GetReferralsByProviderPaged
) : BaseViewModel(){
    private val _allReferrals = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    private val _uiState = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val uiState: StateFlow<List<ReferralWithNames>> = _uiState.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val _referralStatus = MutableStateFlow<ReferralStatus?>(null)
    val referralStatus: StateFlow<ReferralStatus?> = _referralStatus.asStateFlow()
    private var allReferralsLoaded = false
    private var lastReferralViewModel: Referral? = null

    private var referralsJob: Job? = null
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
                fetchAllReferrals()
            }
            _searchText
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    filterReferralsLocally(query)
                }
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
    }

    fun updateReferralStatus(newStatus: ReferralStatus?) {
        _referralStatus.value = newStatus
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

    private fun fetchAllReferrals(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob =launchCatching {
            val userData = _userDataStore.value
            val flow = when(userData) {
                is UserData.Client -> getReferralsByClient(currentUserId)
                is UserData.Provider -> getReferralsByProvider(currentUserId)
                else -> null
            }

            flow?.collect { referrals ->
                val uniqueIds = referrals.map { referral ->
                    val otherId = if (userData is UserData.Client) referral.providerId else referral.clientId
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
                val referralsWithNames = referrals.map { referral ->
                    val otherId = if (userData is UserData.Client) referral.providerId else referral.clientId
                    ReferralWithNames(
                        referral = referral,
                        otherPartyName = nameCache[otherId] ?: ""
                    )
                }
                _allReferrals.value = referralsWithNames
                //si there was algo escrito
                filterReferralsLocally(_searchText.value)
                _isLoading.value = false
            }
        }
        referralsJob?.invokeOnCompletion { if (it is CancellationException) _isLoading.value = false }
    }

    private fun loadInitialReferralsByClient(pageSize: Long=3){
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
                _allReferrals.value = result
                lastReferralViewModel = lastReferral
                allReferralsLoaded = result.size < pageSize
                val lastTime = lastReferral?.createdAt ?: System.currentTimeMillis()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForNewReferralsByClient(clientId: String, since: Long) {
        realTimeJob?.cancel()
        realTimeJob = launchCatching {

        }
    }

    private fun filterReferralsLocally(query: String) {
        val queryNormalized = query.normalizeName()
        val allEnriched = _allReferrals.value
        if (queryNormalized.isEmpty()) {
            _uiState.value = allEnriched
        } else {
            // Busca en cualquier parte del nombre (contains)
            _uiState.value = _allReferrals.value.filter { item ->
                item.referral.nameLowercase.contains(queryNormalized)
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