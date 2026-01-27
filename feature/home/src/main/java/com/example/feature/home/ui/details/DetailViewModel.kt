package com.example.feature.home.ui.details

import android.util.Log
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.GetReferralsByClientByProvider
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val getUser: GetUser,
    private val getReferralsByClientByProvider: GetReferralsByClientByProvider
) : BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    val currentUserId
        get() = currentUserIdUseCase()

    fun loadUserInformation(uid: String?){
        if(uid == null) return
        try {
            launchCatching {
                _userDataStore.value = getUser(uid)
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
}