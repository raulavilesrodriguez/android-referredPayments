package com.example.feature.home.ui.paymentsMovement

import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClient
import com.avilesrodriguez.domain.usecases.GetReferralsByProvider
import com.avilesrodriguez.domain.usecases.GetUserFlow
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentsMovementViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getReferralsByProvider: GetReferralsByProvider,
    private val getReferralsByClient: GetReferralsByClient,
    private val getUserFlow: GetUserFlow
): BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore
    private val _referralsProvider = MutableStateFlow<List<Referral>>(emptyList())
    val referralsProvider: StateFlow<List<Referral>> = _referralsProvider
    private val _referralsClient = MutableStateFlow<List<Referral>>(emptyList())
    val referralsClient: StateFlow<List<Referral>> = _referralsClient
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var referralsJob: Job? = null

    val currentUserId
        get() = currentUserIdUseCase()

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
                is UserData.Provider ->{
                    loadReferralsByProvider()
                }
                is UserData.Client -> {
                    loadReferralsByClient()
                }
            }
        }
    }

    private fun loadReferralsByProvider(){
        _isLoading.value = true
        referralsJob?.cancel()
        referralsJob = launchCatching {
            getReferralsByProvider(currentUserId)
                .collect { referrals ->
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
                    _referralsClient.value = referrals
                    _isLoading.value = false
                }
        }
    }
}