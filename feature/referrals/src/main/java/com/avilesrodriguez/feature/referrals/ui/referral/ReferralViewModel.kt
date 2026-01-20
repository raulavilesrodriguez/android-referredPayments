package com.avilesrodriguez.feature.referrals.ui.referral

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralById
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val getReferralById: GetReferralById
) : BaseViewModel() {
    private val _referralState = MutableStateFlow(Referral())
    val referralState: StateFlow<Referral> = _referralState.asStateFlow()
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    var clientWhoReferred by mutableStateOf<UserData?>(null)
    var providerThatReceived by mutableStateOf<UserData?>(null)

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if(hasUser()){
                _userDataStore.value = getUser(currentUserId)
            }
        }
    }

    fun loadReferralInformation(referralId: String){
        try {
            launchCatching {
                val referral = getReferralById(referralId)
                _referralState.value = referral?: Referral()
            }
        } catch (ie: Throwable){
            Log.e("ReferralViewModel", "Error loading referral information", ie)
        }
    }

}