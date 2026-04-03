package com.example.feature.home.ui.graphicsMetrics

import androidx.lifecycle.viewModelScope
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.referral.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.referral.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.user.GetUserFlow
import com.avilesrodriguez.domain.usecases.account.HasUser
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import com.example.feature.home.models.ReferralPercentageMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUserFlow: GetUserFlow,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val getReferralsByClient: GetReferralsByClient,
) : BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _uiStateReferralsMetrics = MutableStateFlow(ReferralMetrics())
    val uiStateReferralsMetrics: StateFlow<ReferralMetrics> = _uiStateReferralsMetrics.asStateFlow()
    private val _referralsProvider = MutableStateFlow<List<Referral>>(emptyList())
    private val _referralsClient = MutableStateFlow<List<Referral>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var referralsJob: Job? = null
    val currentUserId get() = currentUserIdUseCase()
    val referralsConversionProvider: StateFlow<Double> = combine(
        _userDataStore,
        _referralsProvider
    ) { user, referrals ->
        val provider = user as? UserData.Provider
        if (provider != null && referrals.isNotEmpty()) {
            provider.totalPayouts.toDouble() / referrals.size
        } else {
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val costByReferralProvider: StateFlow<Double> = _userDataStore.map { user ->
        val provider = user as? UserData.Provider
        if (provider != null && provider.totalPayouts > 0) {
            provider.moneyPaid / provider.totalPayouts
        } else {
            0.0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val winByReferralClient: StateFlow<Double> = combine(
        _userDataStore,
        _referralsClient
    ) { user, referrals ->
        val client = user as? UserData.Client
        if (client != null && referrals.isNotEmpty()) {
            client.moneyEarned / referrals.size
        } else {
            0.0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val percentageMetrics: StateFlow<ReferralPercentageMetrics> = _uiStateReferralsMetrics.map { metrics ->
        val total = metrics.totalReferrals.toDouble()
        if (total > 0) {
            ReferralPercentageMetrics(
                percentageRejected = metrics.rejectedReferrals.toDouble() / total,
                percentagePaid = metrics.paidReferrals.toDouble() / total,
                percentageProcessing = metrics.processingReferrals.toDouble() / total,
                percentagePending = metrics.pendingReferrals.toDouble() / total
            )
        } else {
            ReferralPercentageMetrics()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReferralPercentageMetrics()
    )

    init {
        launchCatching {
            if(hasUser()){
                launch {
                    getUserFlow(currentUserId).collect {
                        _userDataStore.value = it
                    }
                }
            }
            val user = _userDataStore.filterNotNull().first()
            when(user){
                is UserData.Provider -> loadReferralMetricsProvider()
                is UserData.Client -> loadReferralMetricsClient()
            }
        }
    }

    private fun loadReferralMetricsClient(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByClient(currentUserId).collect { referrals ->
                _referralsClient.value = referrals
                _uiStateReferralsMetrics.value = ReferralMetrics(
                    totalReferrals = referrals.size,
                    pendingReferrals = referrals.count { it.status == ReferralStatus.PENDING },
                    processingReferrals = referrals.count { it.status == ReferralStatus.PROCESSING },
                    rejectedReferrals = referrals.count { it.status == ReferralStatus.REJECTED },
                    paidReferrals = referrals.count { it.status == ReferralStatus.PAID }
                )
                _isLoading.value = false
            }
        }
    }

    private fun loadReferralMetricsProvider(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByProvider(currentUserId).collect { referrals ->
                _referralsProvider.value = referrals
                _uiStateReferralsMetrics.value = ReferralMetrics(
                    totalReferrals = referrals.size,
                    pendingReferrals = referrals.count { it.status == ReferralStatus.PENDING },
                    processingReferrals = referrals.count { it.status == ReferralStatus.PROCESSING },
                    rejectedReferrals = referrals.count { it.status == ReferralStatus.REJECTED },
                    paidReferrals = referrals.count { it.status == ReferralStatus.PAID }
                )
                _isLoading.value = false
            }
        }
    }

}