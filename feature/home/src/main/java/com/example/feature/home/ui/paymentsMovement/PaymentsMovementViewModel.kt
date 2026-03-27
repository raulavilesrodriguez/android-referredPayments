package com.example.feature.home.ui.paymentsMovement

import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClientPaged
import com.avilesrodriguez.domain.usecases.GetReferralsByClientSince
import com.avilesrodriguez.domain.usecases.GetReferralsByProviderPaged
import com.avilesrodriguez.domain.usecases.GetReferralsByProviderSince
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.GetUserFlow
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class PaymentsMovementViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUserFlow: GetUserFlow,
    private val getUser: GetUser,
    private val getReferralsByClientSince: GetReferralsByClientSince,
    private val getReferralsByClientPaged: GetReferralsByClientPaged,
    private val getReferralsByProviderSince: GetReferralsByProviderSince,
    private val getReferralsByProviderPaged: GetReferralsByProviderPaged
): BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _referralsProvider = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val referralsProvider: StateFlow<List<ReferralWithNames>> = _referralsProvider
    private val _referralsClient = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    val referralsClient: StateFlow<List<ReferralWithNames>> = _referralsClient
    // Filtros de fecha
    private val _dateFrom = MutableStateFlow<Long?>(null)
    val dateFrom: StateFlow<Long?> = _dateFrom.asStateFlow()
    private val _dateTo = MutableStateFlow<Long?>(null)
    val dateTo: StateFlow<Long?> = _dateTo.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var allReferralsLoaded = false
    private var lastReferralViewModel: Referral? = null
    private var paginationJob: Job? = null
    private var realTimeJob: Job? = null
    private val nameCache = ConcurrentHashMap<String, String>()

    val currentUserId get() = currentUserIdUseCase()

    init {
        launchCatching{
            if(hasUser()){
                val user = getUser(currentUserId)
                _userDataStore.value = user

                launch {
                    getUserFlow(currentUserId)
                        .filterNotNull()
                        .collect {
                            _userDataStore.value = it
                        }
                }
                launch{
                    combine(_dateFrom, _dateTo){_, _ -> Unit}
                        .collect {
                            lastReferralViewModel = null
                            allReferralsLoaded = false

                            val currentUser = _userDataStore.value ?: return@collect
                            when (currentUser) {
                                is UserData.Provider -> loadInitialReferralsByProvider()
                                is UserData.Client -> loadInitialReferralsByClient()
                            }
                        }
                }

            }
        }
    }

    fun onDateFromChange(date: Long?) { _dateFrom.value = date }
    fun onDateToChange(date: Long?) { _dateTo.value = date }

    private suspend fun transformToReferralsWithName(
        referrals:List<Referral>,
        isProvider: Boolean
    ): List<ReferralWithNames>{
        val paidReferrals = referrals.filter { it.status == ReferralStatus.PAID }
        val uniqueIds = paidReferrals.map { if (isProvider) it.clientId else it.providerId  }.distinct()
        val missingIds = uniqueIds.filter { !nameCache.containsKey(it) }

        if (missingIds.isNotEmpty()) {
            coroutineScope {
                missingIds.map { id ->
                    async { id to (getUser(id)?.name ?: "") }
                }.awaitAll().forEach { (id, name) ->
                    nameCache[id] = name
                }
            }
        }

        return paidReferrals.map { referral ->
                val otherId = if (isProvider) referral.clientId else referral.providerId
                ReferralWithNames(
                    referral = referral,
                    otherPartyName = nameCache[otherId] ?: ""
                )
            }
            .sortedByDescending { it.referral.paidAt }
    }

    private fun loadInitialReferralsByClient(pageSize: Long=10){
        _isLoading.value = true
        paginationJob?.cancel()
        paginationJob = launchCatching {
            try {
                val(referrals, lastReferral) = getReferralsByClientPaged(
                    clientId = currentUserId,
                    pageSize = pageSize,
                    lastReferral = null,
                    fromDate = _dateFrom.value,
                    toDate = _dateTo.value,
                    status = ReferralStatus.PAID.name,
                    isPaymentsScreen = true
                )
                val result = transformToReferralsWithName(referrals, false)
                _referralsClient.value = result
                lastReferralViewModel = lastReferral
                allReferralsLoaded = result.size < pageSize
                val lastTime = lastReferral?.paidAt ?: System.currentTimeMillis()
                listenForNewReferralsByClient(currentUserId, lastTime)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForNewReferralsByClient(clientId: String, since: Long){
        realTimeJob?.cancel()
        realTimeJob = launchCatching {
            getReferralsByClientSince(
                clientId = clientId,
                since = since,
                toDate = _dateTo.value,
                isPaymentsScreen = true
            )
                .flowOn(Dispatchers.IO)
                .collect { newReferrals ->
                    if(newReferrals.isNotEmpty()){
                        val currentReferrals = _referralsClient.value
                        val updatedReferrals = (transformToReferralsWithName(newReferrals, false) + currentReferrals).distinctBy { it.referral.id }
                        _referralsClient.value = updatedReferrals
                    }
                }
        }
    }

    fun loadMoreReferralsByClient(pageSize: Long=2){
        if(allReferralsLoaded || paginationJob?.isActive == true) return
        paginationJob?.cancel()
        paginationJob = launchCatching {
            if(lastReferralViewModel == null) {
                allReferralsLoaded = true
                return@launchCatching
            }
            val (olderReferrals, lastReferral) = getReferralsByClientPaged(
                clientId = currentUserId,
                pageSize = pageSize,
                lastReferral = lastReferralViewModel,
                fromDate = _dateFrom.value,
                toDate = _dateTo.value,
                status = ReferralStatus.PAID.name,
                isPaymentsScreen = true
            )
            if(olderReferrals.isNotEmpty()){
                val currentReferrals = _referralsClient.value
                _referralsClient.value = (currentReferrals + transformToReferralsWithName(olderReferrals, false)).distinctBy { it.referral.id }
            }
            lastReferralViewModel = lastReferral
            allReferralsLoaded = olderReferrals.size < pageSize
        }
    }

    private fun loadInitialReferralsByProvider(pageSize: Long=10){
        _isLoading.value = true
        paginationJob?.cancel()
        paginationJob = launchCatching {
            try {
                val(referrals, lastReferral) = getReferralsByProviderPaged(
                    providerId = currentUserId,
                    pageSize = pageSize,
                    lastReferral = null,
                    fromDate = _dateFrom.value,
                    toDate = _dateTo.value,
                    status = ReferralStatus.PAID.name,
                    isPaymentsScreen = true
                )
                val result = transformToReferralsWithName(referrals, true)
                _referralsProvider.value = result
                lastReferralViewModel = lastReferral
                allReferralsLoaded = result.size < pageSize
                val lastTime = lastReferral?.paidAt ?: System.currentTimeMillis()
                listenForNewReferralsByProvider(currentUserId, lastTime)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun listenForNewReferralsByProvider(providerId: String, since: Long){
        realTimeJob?.cancel()
        realTimeJob = launchCatching {
            getReferralsByProviderSince(
                providerId = providerId,
                since = since,
                toDate = _dateTo.value,
                isPaymentsScreen = true
            )
                .flowOn(Dispatchers.IO)
                .collect { newReferrals ->
                    if(newReferrals.isNotEmpty()){
                        val currentReferrals = _referralsProvider.value
                        val updatedReferrals = (transformToReferralsWithName(newReferrals, true) + currentReferrals).distinctBy { it.referral.id }
                        _referralsProvider.value = updatedReferrals
                    }
                }
        }
    }

    fun loadMoreReferralsByProvider(pageSize: Long = 2){
        if(allReferralsLoaded || paginationJob?.isActive == true) return
        paginationJob?.cancel()
        paginationJob = launchCatching {
            if(lastReferralViewModel == null) {
                allReferralsLoaded = true
                return@launchCatching
            }
            val (olderReferrals, lastReferral) = getReferralsByProviderPaged(
                providerId = currentUserId,
                pageSize = pageSize,
                lastReferral = lastReferralViewModel,
                fromDate = _dateFrom.value,
                toDate = _dateTo.value,
                status = ReferralStatus.PAID.name,
                isPaymentsScreen = true
            )
            if(olderReferrals.isNotEmpty()){
                val currentReferrals = _referralsProvider.value
                _referralsProvider.value = (currentReferrals + transformToReferralsWithName(olderReferrals, true)).distinctBy { it.referral.id }
            }
            lastReferralViewModel = lastReferral
            allReferralsLoaded = olderReferrals.size < pageSize
        }
    }
}
