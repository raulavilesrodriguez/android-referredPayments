package com.example.feature.home.ui.paymentsMovement

import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.GetUserFlow
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class PaymentsMovementViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val getReferralsByClient: GetReferralsByClient,
    private val getUserFlow: GetUserFlow,
    private val getUser: GetUser
): BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    private val _referralsProvider = MutableStateFlow<List<ReferralWithNames>>(emptyList())
    private val _referralsClient = MutableStateFlow<List<ReferralWithNames>>(emptyList())

    // Filtros de fecha
    private val _dateFrom = MutableStateFlow<Long?>(null)
    val dateFrom: StateFlow<Long?> = _dateFrom.asStateFlow()

    private val _dateTo = MutableStateFlow<Long?>(null)
    val dateTo: StateFlow<Long?> = _dateTo.asStateFlow()

    // Referidos filtrados
    val filteredReferralsProvider = combine(_referralsProvider, _dateFrom, _dateTo) { referrals, from, to ->
        filterByDate(referrals, from, to)
    }

    val filteredReferralsClient = combine(_referralsClient, _dateFrom, _dateTo) { referrals, from, to ->
        filterByDate(referrals, from, to)
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var referralsJob: Job? = null

    private val nameCache = ConcurrentHashMap<String, String>()

    val currentUserId get() = currentUserIdUseCase()

    init {
        launchCatching{
            if(hasUser()){
                launch {
                    getUserFlow(currentUserId).collect {
                        _userDataStore.value = it
                    }
                }
            }
            val user = _userDataStore.filterNotNull().first()
            when(user){
                is UserData.Provider -> loadReferralsByProvider()
                is UserData.Client -> loadReferralsByClient()
            }
        }
    }

    fun onDateFromChange(date: Long?) { _dateFrom.value = date }
    fun onDateToChange(date: Long?) { _dateTo.value = date }

    private fun filterByDate(referrals: List<ReferralWithNames>, from: Long?, to: Long?): List<ReferralWithNames> {
        return referrals.filter { referral ->
            val paidAt = referral.referral.paidAt
            val matchFrom = from == null || paidAt >= from
            val matchTo = to == null || paidAt <= (to + 86399999) // Incluir todo el día hasta las 23:59:59
            matchFrom && matchTo
        }
    }

    private fun loadReferralsByProvider() {
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByProvider(currentUserId).collect { referrals ->
                val uniqueIds = referrals.map { it.clientId }.distinct()
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

                _referralsProvider.value = referrals
                    .filter { it.status == ReferralStatus.PAID }
                    .map { referral ->
                        ReferralWithNames(
                            referral = referral,
                            otherPartyName = nameCache[referral.clientId] ?: ""
                        )
                    }.sortedByDescending { it.referral.paidAt }
                _isLoading.value = false
            }
        }
    }

    private fun loadReferralsByClient() {
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByClient(currentUserId).collect { referrals ->
                val uniqueIds = referrals.map { it.providerId }.distinct()
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

                _referralsClient.value = referrals
                    .filter { it.status == ReferralStatus.PAID }
                    .map { referral ->
                        ReferralWithNames(
                            referral = referral,
                            otherPartyName = nameCache[referral.providerId] ?: ""
                        )
                    }.sortedByDescending { it.referral.paidAt }
                _isLoading.value = false
            }
        }
    }
}
