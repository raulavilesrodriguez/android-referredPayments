package com.example.feature.home.ui.details

import android.util.Log
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClientByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.presentation.ext.getById
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val getUser: GetUser,
    private val getReferralsByClientByProvider: GetReferralsByClientByProvider
) : BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _uiStateReferrals = MutableStateFlow<List<Referral>>(emptyList())
    val uiStateReferrals: StateFlow<List<Referral>> = _uiStateReferrals.asStateFlow()
    private val _allReferrals = MutableStateFlow<List<Referral>>(emptyList())
    private val _uiStateReferralsMetrics = MutableStateFlow(ReferralMetrics())
    val uiStateReferralsMetrics: StateFlow<ReferralMetrics> = _uiStateReferralsMetrics.asStateFlow()
    private val _selectedStatus = MutableStateFlow<ReferralStatus?>(null)
    val selectedStatus: StateFlow<ReferralStatus?> = _selectedStatus

    private var referralsJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

    fun loadUserInformation(uid: String?){
        if(uid == null) return
        try {
            launchCatching {
                val user = getUser(uid)
                _userDataStore.value = user
                if(user is UserData.Client) loadReferralsByClientByProvider(user.uid)
            }
        } catch (ie: Throwable){
            Log.e("DetailViewModel", "Error loading user information", ie)
        }
    }

    fun onAddReferClick(uid: String, openScreen: (String) -> Unit){
        openScreen(NavRoutes.NEW_REFERRAL.replace("{id}", uid))
    }

    fun onReferClick(id: String, openScreen: (String) -> Unit){
        openScreen(NavRoutes.REFERRAL_DETAIL.replace("{id}", id))
    }

    private fun loadReferralsByClientByProvider(uidClient: String){
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByClientByProvider(uidClient, currentUserId)
                .collect { referrals ->
                    _uiStateReferrals.value = referrals
                    _allReferrals.value = referrals

                    _uiStateReferralsMetrics.value = ReferralMetrics(
                        totalReferrals = referrals.size,
                        pendingReferrals = referrals.count { it.status == ReferralStatus.PENDING },
                        processingReferrals = referrals.count { it.status == ReferralStatus.PROCESSING },
                        rejectedReferrals = referrals.count { it.status == ReferralStatus.REJECTED },
                        paidReferrals = referrals.count { it.status == ReferralStatus.PAID }
                    )
                }
        }
    }

    fun filterReferralsByStatus(status:Int){
        _selectedStatus.value = ReferralStatus.getById(status)
        updateFilteredList()
    }

    private fun updateFilteredList() {
        val status = _selectedStatus.value
        _uiStateReferrals.value = if (status == null) {
            _allReferrals.value
        } else {
            _allReferrals.value.filter { it.status == status }
        }
    }
}